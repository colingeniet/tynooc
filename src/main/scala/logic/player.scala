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


class Player(val fabricTown: Town = Game.world.towns(0)) {
  var trains: HashSet[Train] = HashSet()
  var carriages: HashSet[Carriage] = HashSet()
  var engines: HashSet[Engine] = HashSet()

  var money: Double = 0

  def travels: HashSet[Travel] = Game.world.travelsOf(this)
  def addMoney(m: Double): Unit = money += m

  def carriagesAvailable: HashSet[Carriage] = carriages.filter(!_.isUsed)

  def carriagesStoredAt(town: Town): HashSet[Carriage] =
    carriages.filter(c => !c.isUsed && c.town == town)

  def enginesAvailable: HashSet[Engine] = engines.filter(!_.isUsed)

  def enginesStoredAt(town: Town): HashSet[Engine] =
    engines.filter(c => !c.isUsed && c.town == town)

  def buyEngine(name: String): Unit = {
    var model = EngineModel(name)
    if (model.price <= money) {
      this.money -= model.price
      engines.add(new Engine(model, fabricTown))
    }
  }

  def buyCarriage(name: String): Unit = {
    var model = CarriageModel(name)
    if (model.price <= money) {
      this.money -= model.price
      carriages.add(new Carriage(model, fabricTown))
    }
  }

  def createTrainFromEngine(engine: Engine): Train = {
    if (!ownsEngine(engine)) {
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

  def addCarriageToTrain(train: Train, carriage: Carriage): Unit = {
    if (!ownsTrain(train)) {
      throw new IllegalArgumentException("Player doesn’t own the train")
    }
    if (train.onRoute) {
      throw new IllegalArgumentException("Train is in use")
    }
    if (!ownsCarriage(carriage)) {
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

  def removeCarriageFromTrain(train: Train): Unit = {
    if (!ownsTrain(train)) {
      throw new IllegalArgumentException("Player doesn’t own the train")
    }
    if (train.onRoute) {
      throw new IllegalArgumentException("Train is in use")
    }
    var carriage: Carriage = train.removeCarriage()
    carriage.train = None
    carriage.town = train.town
  }

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

  def launchTravel(train:Train, to:Town): Unit = {
    if (!ownsTrain(train)) {
      throw new IllegalArgumentException("Player doesn’t own the train")
    }
    if (train.onRoute) {
      throw new IllegalArgumentException("Train is in use")
    }
    if (train.tooHeavy) {
      throw new IllegalArgumentException("Train is too heavy")
    }
    var routes = Game.world.findPath(train.town, to) match {
      case None => throw new PathNotFound
      case Some(routes) => routes
    }
    var travel = new Travel(train, routes, this)
    train.travel = Some(travel)
    Game.world.addTravel(travel)
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
  def ownsCarriage(carriage: Carriage): Boolean = carriages.contains(carriage)
  def ownsEngine(engine: Engine): Boolean = engines.contains(engine)

  def update(dt: Double): Unit = {}
}
