package logic.company

import scalafx.beans.property._
import scalafx.collections._

import scala.collection.mutable.HashSet

import logic.vehicle._
import logic.travel._
import logic.town._
import logic.game._
import logic.world._

/** An object to manage prices. */
object PriceSimulation {
  /** Returns the price of an upgrade.
    *
    * @param from The old model.
    * @param to The upgraded model.
    */
  def upgradePrice(from: VehicleUnit, to: String): Double = {
    (from.modelNameMap(to).price - from.model.price) * 1.2
  }
}

final case class IllegalOwnerException(
  private val message: String = "",
  private val cause: Throwable = None.orNull)
extends Exception(message, cause)


/** A company.
  *
  * @param name The name of the company.
  * @param fabricTown The town in which the company rolling stock is produced.
  */
class Company(var name: String, val fabricTown: Town) {
  /** The company trains. */
  val vehicles: ObservableBuffer[Vehicle] = ObservableBuffer()
  /** The company carriages. */
  val vehicleUnits: ObservableBuffer[VehicleUnit] = ObservableBuffer()
  /** The company money. */
  val money: DoubleProperty = DoubleProperty(0)
  /** Current travels for this company. */
  def travels: HashSet[Travel] = Game.world.travelsOf(this)

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
        case v: Vehicle => vehicles.add(v)
        case _ => ()
      }
      vehicleUnits.add(vehicle)
    }
  }


  /** Adds a carriage at the tail of an existing train.
    *
    * @param train The train to extend.
    * @param carriage The carriage to add to <code>train</code>.
    */
  def addCarriageToTrain(train: Engine, carriage: Carriage): Unit = {
    if (!ownsVehicle(train)) {
      throw new IllegalOwnerException("Company doesn't own the train")
    }
    if (!ownsVehicle(carriage)) {
      throw new IllegalOwnerException("Company doesn't own the carriage")
    }
    train.addCarriage(carriage)
  }

  /** Removes the tail carriage of an existing train.
    *
    * @param train The train.
    */
  def removeCarriageFromTrain(train: Engine): Unit = {
    if (!ownsVehicle(train)) {
      throw new IllegalOwnerException("Company doesn't own the train")
    }
    train.removeCarriage()
  }

  /** Completely disassemble an existing train.
    *
    * @param train The train to disassemble.
    */
  def disassembleTrain(train: Engine): Unit = {
    if (!ownsVehicle(train)) {
      throw new IllegalOwnerException("Company doesn't own the train")
    }
    train.disassemble()
  }

  /** Start a new travel (starting town will be the train's current town.)
    *
    * @param train The train to launch.
    * @param to The destination of the travel.
    */
  def launchTravel(vehicle: Vehicle, to: Town, _onCompleted: () => Unit = () => ()): Unit = {
    if (!ownsVehicle(vehicle)) {
      throw new IllegalOwnerException("Company doesn't own the train")
    }
    val travel = vehicle.launchTravel(to)
    travel.onCompleted = _onCompleted
    Game.world.addTravel(travel)
  }

  /** Upgrade a vehicle (a carriage or an engine).
    *
    * @param old The vehicle to upgrade.
    * @param model The upgraded model.
    */
  def upgrade(old: VehicleUnit, model: String): Unit = {
    if (!ownsVehicle(old)) {
      throw new IllegalOwnerException("Company doesn't own the vehicle")
    }
    if (money() >= PriceSimulation.upgradePrice(old, model)) {
      debit(PriceSimulation.upgradePrice(old, model))
      old.upgradeTo(model)
    }
  }

  /** Returns true if this company owns <code>vehicle</code>. */
  def ownsVehicle(vehicle: VehicleUnit): Boolean = vehicle.owner == this
}
