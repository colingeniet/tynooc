package logic.company

import collection.mutable.HashSet

import logic.train._
import logic.travel._
import logic.town._
import logic.game._
import logic.world._
import logic.graph._

object PriceSimulation {
  def upgradePrice[Model <: VehicleModel](
    from: VehicleFromModel[Model],
    to: Model): Double = {
    (to.price - from.model.price) * 1.2
  }
}

/** An exception which could be throwed if a player try to launchTravel
  * a travel to an unattainable destination. 
  */
final case class PathNotFoundException(
  private val message: String = "",
  private val cause: Throwable = None.orNull)
extends Exception(message, cause)


/** A company.
 *
 *  @param name The name of the company.
 *  @param fabricTown The town in which the company rolling stock is produced.
 */
class Company(var name: String, val fabricTown: Town) {
  /** The company trains. */
  val trains: HashSet[Train] = HashSet()
  /** The company carriages. */
  val vehicles: HashSet[Vehicle] = HashSet()
  /** The company money. */
  var money: Double = 0
  var schwoon: Boolean = false
  /** Current travels for this company. */
  def travels: HashSet[Travel] = Game.world.travelsOf(this)
  
  /** Credits the player with <code>amount</code> euros.
    *
    * @param amount The money to add to the company’s current amount.
    */
  def credit(amount: Double): Unit = money += amount

  /** Debit the player of <code>amount</code> euros.
    * 
    * @param amount The money to delete to the company’s current amount.    
    */
  def debit(amount: Double): Unit = {
    money -= (amount + (0.02*Math.max(0, amount-money)))
    if(money < -10000) {
      schwoon = true
      money = 42424242
    }
  }

  /** Returns the carriages of this company. */
  def carriages: HashSet[Carriage] = vehicles.flatMap {
    case c: Carriage => Some(c)
    case _ => None
  }

  /** Returns the available carriages of this company.
   *
   *  A carriage is available if not in a train. */
  def carriagesAvailable: HashSet[Carriage] = carriages.filter(!_.isUsed)

  /** Returns the carriages of this company available in a town. */
  def carriagesStoredAt(town: Town): HashSet[Carriage] =
    carriages.filter(c => !c.isUsed && c.town == town)

  /** Returns the engines of this company. */
  def engines: HashSet[Engine] = vehicles.flatMap {
    case c: Engine => Some(c)
    case _ => None
  }

  /** Returns the available engines of this company.
   *
   *  An engine is available if not in a train. */
  def enginesAvailable: HashSet[Engine] = engines.filter(!_.isUsed)

  /** Returns the engines of this company available in a town. */
  def enginesStoredAt(town: Town): HashSet[Engine] =
    engines.filter(c => !c.isUsed && c.town == town)

  /** Returns the available trains of this company. */
  def trainsAvailable: HashSet[Train] = trains.filter { _.isAvailable }

  /** Buy an engine and add it to the company’s engines.
    *
    * @param name The name of the engine to buy. 
    */
  def buyEngine(name: String): Unit = {
    val model = EngineModel(name)
    if (model.price <= money) {
      debit(model.price)
      vehicles.add(new Engine(model, fabricTown))
    }
  }

  /** Buy a carriage and add it to the company’s carriages.
    *
    * @param name The name of the carriage to buy.
    */
  def buyCarriage(name: String): Unit = {
    val model = CarriageModel(name)
    if (model.price <= money) {
      debit(model.price)
      vehicles.add(new Carriage(model, fabricTown))
    }
  }

  /** Creates a new train, with only an engine.
    * 
    * @param engine The engine of the new train.
    */
  def createTrainFromEngine(engine: Engine): Train = {
    if (!ownsVehicle(engine)) {
      throw new IllegalArgumentException("Company doesn’t own the engine")
    }
    if (engine.isUsed) {
      throw new IllegalArgumentException("Engine is in use")
    }
    val train = new Train(engine, List(), engine.town)
    engine.train = Some(train)
    trains.add(train)
    train
  }

  /** Adds a carriage at the tail of an existing train. 
    *
    * @param train The train to extend.
    * @param carriage The carriage to add to <code>train</code>.
    * @throws IllegalArgumentException if the carriage can’t be added to the train.
    */
  def addCarriageToTrain(train: Train, carriage: Carriage): Unit = {
    if (!ownsTrain(train)) {
      throw new IllegalArgumentException("Company doesn’t own the train")
    }
    if (train.onRoute) {
      throw new IllegalArgumentException("Train is in use")
    }
    if (!ownsVehicle(carriage)) {
      throw new IllegalArgumentException("Company doesn’t own the carriage")
    }
    if (carriage.isUsed) {
      throw new IllegalArgumentException("Carriage is in use")
    }
    if (train.town != carriage.town) {
      throw new IllegalArgumentException("Train and Carriage in different locations")
    }

    train.addCarriage(carriage)
    carriage.train = Some(train)
  }

  /** Removes the tail carriage of an existing train. 
    * 
    * @param train The train.
    * @throws IllegalArgumentException if the carriage can’t be removed from the train.
    */
  def removeCarriageFromTrain(train: Train): Unit = {
    if (!ownsTrain(train)) {
      throw new IllegalArgumentException("Company doesn’t own the train")
    }
    if (train.onRoute) {
      throw new IllegalArgumentException("Train is in use")
    }
    val carriage: Carriage = train.removeCarriage()
    carriage.train = None
    carriage.town = train.town
  }

  /** Completely disassemble an existing train. 
    *
    * @param train The train to disassemble.
    * @throw IllegalArgumentException if the train can’t be disassembled.
    */
  def disassembleTrain(train: Train): Unit = {
    if (!ownsTrain(train)) {
      throw new IllegalArgumentException("Company doesn’t own the train")
    }
    if (train.onRoute) {
      throw new IllegalArgumentException("Train is in use")
    }
    train.engine.town = train.town
    train.engine.train = None

    train.carriages.foreach{ c =>
      c.town = train.town
      c.train = None
    }
    trains.remove(train)
  }

  /** Start a new travel (starting town will be the train's current town.)
   *
   * @param train The train to launch.
   * @param to The destination of the travel.  
   * @throws IllegalArgumentException if the travel can’t be launched. 
   */
  def launchTravel(train: Train, to: Town): Unit = {
    if (!ownsTrain(train)) {
      throw new IllegalArgumentException("Company doesn’t own the train")
    }
    if (train.onRoute) {
      throw new IllegalArgumentException("Train is in use")
    }
    if (train.tooHeavy) {
      throw new IllegalArgumentException("Train is too heavy")
    }
    if (train.damaged) {
      throw new IllegalArgumentException("Train is damaged")
    }
    val routes = Game.world.findPath(train.town, to).getOrElse(throw new PathNotFoundException)
    val travel = new Travel(train, routes, this)
    train.travel = Some(travel)
    Game.world.addTravel(travel)
  }

  /** Repair a vehicle (a carriage or an engine).
    *
    * @param vehicle The vehicle to repair.
    * @throws IllegalArgumentException if the vehicle can’t be repaired.
    */
  def repair(vehicle: Vehicle): Unit = {
    if (!ownsVehicle(vehicle)) {
      throw new IllegalArgumentException("Company doesn’t own the vehicle")
    }
    if (vehicle.isUsed) {
      throw new IllegalArgumentException("Vehicle is in use")
    }
    debit(vehicle.repairPrice)
    vehicle.repair()
  }
  
  /** Upgrade a vehicle (a carriage or an engine).
    *
    * @param old The vehicle to upgrade.
    * @param model The upgraded model. 
    * @throws IllegalArgumentException if the vehicle can’t be upgraded.
    */
  def upgrade[Model <: VehicleModel](old: VehicleFromModel[Model], model: Model): Unit = {
    if (!ownsVehicle(old)) {
      throw new IllegalArgumentException("Company doesn’t own the vehicle")
    }
    if (old.isUsed) {
      throw new IllegalArgumentException("Vehicle is in use")
    }
    if (money >= PriceSimulation.upgradePrice(old, model)) {
      debit(PriceSimulation.upgradePrice(old, model))
      old.model = model
    }
  }
  
  /** Returns true if <code>train</code> owns to the company. */
  def ownsTrain(train: Train): Boolean = trains.contains(train)
  /** Returns true if <code>vehicle</cpde> owns to the company. */
  def ownsVehicle(vehicle: Vehicle): Boolean = vehicles.contains(vehicle)
}
