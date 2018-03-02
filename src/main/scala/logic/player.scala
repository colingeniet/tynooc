package logic.player

import collection.mutable.HashSet

import logic.train._
import logic.travel._
import logic.town._
import logic.game._
import logic.world._
import logic.graph._

object PriceSimulation {
  def upgradePrice(from: Engine, to: EngineModel): Double = {
    to.price-from.model.price
  }

  def upgradePrice(from: Carriage, to: CarriageModel): Double = {
    to.price-from.model.price
  }
}

final case class PathNotFound(
  private val message: String = "",
  private val cause: Throwable = None.orNull)
extends Exception(message, cause)


/** A player.
 *
 *  @param fabricTown the town in which the player rolling stock is produced.
 */
class Player(val fabricTown: Town = Game.world.towns(0)) {
  /** The player trains. */
  val trains: HashSet[Train] = HashSet()
  /** The player carriages. */
  val vehicles: HashSet[Vehicle] = HashSet()

  var money: Double = 0

  /** Current travels for this player. */
  def travels: HashSet[Travel] = Game.world.travelsOf(this)

  def addMoney(m: Double): Unit = money += m
  def debit(m: Double): Unit = money -= m

  /** Returns the carriages of this player. */
  def carriages: HashSet[Carriage] = vehicles.flatMap {
    case c: Carriage => Some(c)
    case _ => None
  }

  /** Returns the available carriages of this player.
   *
   *  A carriage is available if not in a train. */
  def carriagesAvailable: HashSet[Carriage] = carriages.filter(!_.isUsed)

  /** Returns the carriages of this player available in a town. */
  def carriagesStoredAt(town: Town): HashSet[Carriage] =
    carriages.filter(c => !c.isUsed && c.town == town)

  /** Returns the engines of this player. */
  def engines: HashSet[Engine] = vehicles.flatMap {
    case c: Engine => Some(c)
    case _ => None
  }

  /** Returns the available engines of this player.
   *
   *  An engine is available if not in a train. */
  def enginesAvailable: HashSet[Engine] = engines.filter(!_.isUsed)

  /** Returns the engines of this player available in a town. */
  def enginesStoredAt(town: Town): HashSet[Engine] =
    engines.filter(c => !c.isUsed && c.town == town)


  def buyEngine(name: String): Unit = {
    val model = EngineModel(name)
    if (model.price <= money) {
      this.money -= model.price
      vehicles.add(new Engine(model, fabricTown))
    }
  }

  def buyCarriage(name: String): Unit = {
    val model = CarriageModel(name)
    if (model.price <= money) {
      this.money -= model.price
      vehicles.add(new Carriage(model, fabricTown))
    }
  }

  /** Creates a new train, with only an engine. */
  def createTrainFromEngine(engine: Engine): Train = {
    if (!ownsVehicle(engine)) {
      throw new IllegalArgumentException("Player doesn’t own the engine")
    }
    if (engine.isUsed) {
      throw new IllegalArgumentException("Engine is in use")
    }
    val train = new Train(engine, List(), engine.town)
    engine.train = Some(train)
    trains.add(train)
    train
  }

  /** Adds a carriage at the tail of an existing train. */
  def addCarriageToTrain(train: Train, carriage: Carriage): Unit = {
    if (!ownsTrain(train)) {
      throw new IllegalArgumentException("Player doesn’t own the train")
    }
    if (train.onRoute) {
      throw new IllegalArgumentException("Train is in use")
    }
    if (!ownsVehicle(carriage)) {
      throw new IllegalArgumentException("Player doesn’t own the carriage")
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

  /** Removes the tail carriage of an existing train. */
  def removeCarriageFromTrain(train: Train): Unit = {
    if (!ownsTrain(train)) {
      throw new IllegalArgumentException("Player doesn’t own the train")
    }
    if (train.onRoute) {
      throw new IllegalArgumentException("Train is in use")
    }
    val carriage: Carriage = train.removeCarriage()
    carriage.train = None
    carriage.town = train.town
  }

  /** Completely disassemble an existing train. */
  def disassembleTrain(train: Train): Unit = {
    if (!ownsTrain(train)) {
      throw new IllegalArgumentException("Player doesn’t own the train")
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

  /** Start a new travel.
   *
   *  Starting town will be the train's current town.
   */
  def launchTravel(train: Train, to: Town): Unit = {
    if (!ownsTrain(train)) {
      throw new IllegalArgumentException("Player doesn’t own the train")
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
    val routes = Game.world.findPath(train.town, to) match {
      case None => throw new PathNotFound
      case Some(routes) => routes
    }
    val travel = new Travel(train, routes, this)
    val distance = routes.foldLeft[Double](0) { _ + _.length }
    debit(train.consumption(distance) * Game.world.fuelPrice)
    train.travel = Some(travel)
    Game.world.addTravel(travel)
  }


  def repair(vehicle: Vehicle): Unit = {
    if (!ownsVehicle(vehicle)) {
      throw new IllegalArgumentException("Player doesn’t own the vehicle")
    }
    if (vehicle.isUsed) {
      throw new IllegalArgumentException("Vehicle is in use")
    }
    vehicle.repair()
  }

  def editEngine(old: Engine, model: EngineModel): Unit = {
    if (money >= PriceSimulation.upgradePrice(old, model)) {
      old.model = model
      money -= PriceSimulation.upgradePrice(old, model)
    }
  }

  def editCarriage(old: Carriage, model: CarriageModel): Unit = {
    if (money >= PriceSimulation.upgradePrice(old, model)) {
      old.model = model
      money -= PriceSimulation.upgradePrice(old, model)
    }
  }

  def ownsTrain(train: Train): Boolean = trains.contains(train)
  def ownsVehicle(vehicle: Vehicle): Boolean = vehicles.contains(vehicle)

  def update(dt: Double): Unit = {}
}
