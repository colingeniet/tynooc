package logic.company

import scalafx.beans.property._
import scalafx.collections._
import scalafx.scene.chart.XYChart

import scala.collection.mutable.HashSet

import logic.vehicle._
import logic.travel._
import logic.town._
import logic.game._
import logic.world._
import logic.good._
import logic.model._
import logic.facility._
import logic.mission._

import collection.mutable.HashMap
import java.io._

/** An object to manage prices. */
object PriceSimulation {
  /** Returns the price of an upgrade.
    *
    * @param from The old model.
    * @param to The upgraded model.
    */
  def upgradePrice[Model <: BuyableModel](from: Upgradable[Model], to: String): Double = {
    (from.modelNameMap(to).price - from.model.price) * 1.2
  }
}


/** A company.
  *
  * @param name The name of the company.
  * @param fabricTown The town in which the company rolling stock is produced.
  */
@SerialVersionUID(0L)
class Company(var _name: String, val fabricTown: Town)
extends Serializable {
  @transient var name: StringProperty = StringProperty(_name)

  /** The company trains. */
  @transient var vehicles: ObservableBuffer[Vehicle] = ObservableBuffer()
  /** The company carriages. */
  @transient var vehicleUnits: ObservableBuffer[VehicleUnit] = ObservableBuffer()
  /** The company money. */
  @transient var money: DoubleProperty = DoubleProperty(0)

  @transient var missions: ObservableBuffer[Mission] = ObservableBuffer()
  @transient var waitingMissions: ObservableBuffer[Mission] = ObservableBuffer()

  val historyLength: Integer = 50
  @transient var moneyHistory: ObservableBuffer[javafx.scene.chart.XYChart.Data[Number, Number]] =
    new ObservableBuffer()
  @transient var vehiclesHistory: ObservableBuffer[javafx.scene.chart.XYChart.Data[Number, Number]] =
    new ObservableBuffer()

  def addMission(m: Mission): Unit = missions += m
  def addWaitingMission(m : Mission): Unit = waitingMissions += m

  def acceptMission(m: Mission): Unit = {
    if(waitingMissions.contains(m)) {
      waitingMissions -= m
      addMission(m)
    }
  }

  def rejectMission(m: Mission): Unit = {
    if(waitingMissions.contains(m)) {
      waitingMissions -= m
      Game.world.sendMission(m)
    }
  }

  def completeMission(m: Mission): Unit = {
    missions -= m
    if(Game.time() <= m.time) {
      this.credit(m.reward)
    } else {
      this.credit(m.reward / 5)
    }
  }

  def advanceMissions(from: Town, to: Town, good: Good, quantity: Double): Unit = {
    var q = quantity
    missions.foreach(_ match {
      case m: HelpMission => {
        if(m.from == from && m.to == to && m.good == good) {
          q -= m.advance(q)
          if(q <= 0) return ()
        }
      }
      case _ => ()
    })
  }

  /** Save current company statistics in history. */
  def historyStep(): Unit = {
    moneyHistory.append(XYChart.Data[Number, Number](
      new java.lang.Double(Game.time()),
      new java.lang.Double(money())))
    if(moneyHistory.length > historyLength) moneyHistory.remove(0)

    vehiclesHistory.append(XYChart.Data[Number, Number](
      new java.lang.Double(Game.time()),
      new java.lang.Integer(vehicleUnits.length)))
    if(vehiclesHistory.length > historyLength) vehiclesHistory.remove(0)
  }

  /** Current travels for this company. */
  def travels: HashSet[Travel] = Game.world.travelsOf(this)

  @transient var travelScripts: ObservableBuffer[Script] = ObservableBuffer()

  /** Credits the player with <code>amount</code> euros.
    *
    * @param amount The money to add to the company's current amount.
    */
  def credit(amount: Double): Unit = money() = money() + amount

  /** Debit the player of <code>amount</code> euros.
    *
    * @param amount The money to delete to the company's current amount.
    */
  def debit(amount: Double): Unit = {
    // interest rate
    money() = money() - (amount + (0.02*Math.max(0, amount-money())))
  }

  /** Returns the carriages of this company. */
  def carriages: ObservableBuffer[Carriage] = vehicleUnits.flatMap {
    case c: Carriage => Some(c)
    case _ => None
  }

  /** Returns the available carriages of this company.
    * A carriage is available if not in a train.
    */
  def carriagesAvailable: ObservableBuffer[Carriage] = carriages.filter(!_.isUsed())

  /** Returns the carriages of this company available in a town.
    *
    * @param town The town.
    */
  def carriagesStoredAt(town: Town): ObservableBuffer[Carriage] =
    carriages.filter(c => !c.isUsed() && c.town() == town)

  /** Returns the engines of this company. */
  def engines: ObservableBuffer[Engine] = vehicleUnits.flatMap {
    case e: Engine => Some(e)
    case _ => None
  }

  /** Returns the available engines of this company.
    * An engine is available if not in a train.
    */
  def enginesAvailable: ObservableBuffer[Engine] = engines.filter(!_.isUsed())

  /** Returns the engines of this company available in a town.
    *
    * @param town The town.
    */
  def enginesStoredAt(town: Town): ObservableBuffer[Engine] =
    engines.filter(e => !e.isUsed() && e.town() == town)


  /** Returns the engines of this company. */
  def trains: ObservableBuffer[Engine] = vehicles.flatMap {
    case e: Engine => Some(e)
    case _ => None
  }

  /** Returns the available trains of this company. */
  def trainsAvailable: ObservableBuffer[Engine] = trains.filter { _.isAvailable() }


  def buy(vehicle: VehicleUnit): Unit = {
    if (vehicle.model.price <= money()) {
      debit(vehicle.model.price)
      vehicle match {
        case v: Vehicle => vehicles.add(v); travelScripts.add(new Script(this, v))
        case _ => ()
      }
      vehicleUnits.add(vehicle)
    }
  }

  def buy(facility: Facility): Unit = {
    if (facility.model.price <= money()) {
      debit(facility.model.price)
      facility.owner() = this
    }
  }

  /** Adds a carriage at the tail of an existing train.
    *
    * @param train The train to extend.
    * @param carriage The carriage to add to <code>train</code>.
    */
  def addCarriageToTrain(train: Engine, carriage: Carriage): Unit = {
    assert(owns(train) && owns(carriage))
    train.addCarriage(carriage)
  }

  /** Removes the tail carriage of an existing train.
    *
    * @param train The train.
    */
  def removeCarriageFromTrain(train: Engine): Unit = {
    assert(owns(train))
    train.removeCarriage()
  }

  /** Completely disassemble an existing train.
    *
    * @param train The train to disassemble.
    */
  def disassembleTrain(train: Engine): Unit = {
    assert(owns(train))
    train.disassemble()
  }

  /** Start a new travel (starting town will be the train's current town.)
    *
    * @param train The train to launch.
    * @param to The destination of the travel.
    */
  def launchTravel(vehicle: Vehicle, to: Town, _onCompleted: () => Unit = () => ()): Unit = {
    assert(owns(vehicle))
    val travel = vehicle.launchTravel(to)
    travel.onCompleted = _onCompleted
    Game.world.addTravel(travel)
  }

  /** Upgrade a vehicle (a carriage or an engine).
    *
    * @param old The vehicle to upgrade.
    * @param model The upgraded model.
    */
  def upgrade[Model <: BuyableModel](old: Upgradable[Model], model: String): Unit = {
    assert(owns(old))
    if (money() >= PriceSimulation.upgradePrice(old, model)) {
      debit(PriceSimulation.upgradePrice(old, model))
      old.upgradeTo(model)
    }
  }

  /** Returns true if this company owns <code>vehicle</code>. */
  def owns[Model <: BuyableModel](thing: Upgradable[Model]): Boolean = {
    thing.owner() == this
  }


  @throws(classOf[IOException])
  private def writeObject(stream: ObjectOutputStream): Unit = {
    stream.defaultWriteObject()
    stream.writeObject(this.name())
    stream.writeObject(this.vehicles.toList)
    stream.writeObject(this.vehicleUnits.toList)
    stream.writeObject(this.money.toDouble)
    stream.writeObject(this.missions.toList)
    stream.writeObject(this.waitingMissions.toList)
    stream.writeObject(this.travelScripts.toList)
  }

  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  private def readObject(stream: ObjectInputStream): Unit = {
    stream.defaultReadObject()
    this.name = StringProperty(stream.readObject().asInstanceOf[String])
    this.vehicles = ObservableBuffer[Vehicle](stream.readObject().asInstanceOf[List[Vehicle]])
    this.vehicleUnits = ObservableBuffer[VehicleUnit](stream.readObject().asInstanceOf[List[VehicleUnit]])
    this.money = DoubleProperty(stream.readObject().asInstanceOf[Double])
    this.missions = ObservableBuffer[Mission](stream.readObject().asInstanceOf[List[Mission]])
    this.waitingMissions = ObservableBuffer[Mission](stream.readObject().asInstanceOf[List[Mission]])
    this.travelScripts = ObservableBuffer[Script](stream.readObject().asInstanceOf[List[Script]])

    // not saved fields
    this.moneyHistory = new ObservableBuffer()
    this.vehiclesHistory = new ObservableBuffer()
  }
}
