package logic.player

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


class Player(val fabricTown: Town = Game.world.towns(0)) {
  var trains: List[Train] = List()
  var carriages: List[Carriage] = List()
  var engines: List[Engine] = List()

  var money: Double = 0

  def travels: List[Travel] = Game.world.travelsOf(this)
  def addMoney(m: Double): Unit = money += m

  def carriagesAvailable: List[Carriage] = carriages.filter(!_.isUsed)

  def carriagesStoredAt(town: Town): List[Carriage] =
    carriages.filter(c => !c.isUsed && c.town == town)

  def enginesAvailable: List[Engine] = engines.filter(!_.isUsed)

  def enginesStoredAt(town: Town): List[Engine] =
    engines.filter(c => !c.isUsed && c.town == town)

  def buyEngine(name: String): Unit = {
    var model = EngineModel(name)
    if (model.price <= money) {
      this.money -= model.price
      engines = new Engine(model, fabricTown) :: engines
    }
  }

  def buyCarriage(name: String): Unit = {
    var model = CarriageModel(name)
    if (model.price <= money) {
      this.money -= model.price
      carriages = new Carriage(model, fabricTown) :: carriages
    }
  }

  def createTrainFromEngine(engine: Engine): Train = {
    if (!engines.contains(engine)) {
      throw new IllegalArgumentException("Player doesn’t own the engine")
    }
    if (engine.isUsed) {
      throw new IllegalArgumentException("Engine is in use")
    }
    val train = new Train(engine, List(), engine.town)
    engine.train = Some(train)
    trains = train :: trains
    train
  }

  def addCarriageToTrain(train: Train, c: Carriage): Unit = {
    if (!trains.contains(train)) {
      throw new IllegalArgumentException("Player doesn’t own the train")
    }
    if (train.onRoute) {
      throw new IllegalArgumentException("Train is in use")
    }
    if (!carriages.contains(c)) {
      throw new IllegalArgumentException("Player doesn’t own the carriage")
    }
    if (c.isUsed) {
      throw new IllegalArgumentException("Carriage is in use")
    }
    if (train.town != c.town) {
      throw new IllegalArgumentException("Train and Carriage in different locations")
    }

    train.addCarriage(c)
    c.train = Some(train)
  }

  def removeCarriageFromTrain(train: Train): Unit = {
    if (!trains.contains(train)) {
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
    if (!trains.contains(train)) {
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
    trains = trains diff List(train)
  }

  def launchTravel(train:Train, to:Town): Unit = {
    if (!trains.contains(train)) {
      throw new IllegalArgumentException("Player doesn’t own the train")
    }
    if (train.onRoute) {
      throw new IllegalArgumentException("Train is in use")
    }
    var routes = Game.world.findPath(train.town, to).get
    var travel = new Travel(train, routes, this, List())
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

  def owns(train: Train):Boolean = trains.contains(train)

  def update(dt: Double): Unit = {}
}
