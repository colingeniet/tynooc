package logic.player

import logic.train._
import logic.travel._
import logic.town._

object PriceSimulation {
  def upgradePrice(from: Engine, to: EngineModel): Double = {
    to.price-from.model.price
  }

  def upgradePrice(from: Carriage, to: CarriageModel): Double = {
    to.price-from.model.price
  }
}


class Player() {
  var trains: List[Train] = List()
  var carriages: List[Carriage] = List()
  var engines: List[Engine] = List()
  var travels: List[Travel] = List()

  var money: Double = 0

  def addMoney(m: Double): Unit = money += m

  def buyEngine(name: String): Unit = {
    var c = EngineModel(name)
    if (c.price <= money) {
      this.money -= c.price
      engines = new Engine(c) :: engines
    }
  }

  def buyCarriage(name: String): Unit = {
    var c = CarriageModel(name)
    if (c.price <= money) {
      this.money -= c.price
      carriages = new Carriage(c) :: carriages
    }
  }

  def createTrainFromEngine(engine: Engine): Train = {
    if (!engines.contains(engine)) {
      throw new IllegalArgumentException("Player doesn’t own the engine")
    }
    val train = new Train(engine, List())
    trains = train :: trains
    engines = engines diff List(engine)
    train
  }

  def addCarriageToTrain(train: Train, c: Carriage): Unit = {
    if (!trains.contains(train)) {
      throw new IllegalArgumentException("Player doesn’t own the train")
    }
    if (!carriages.contains(c)) {
      throw new IllegalArgumentException("Player doesn’t own the carriage")
    }
    train.addCarriage(c)
    carriages = carriages diff List(c)
  }

  def removeCarriageFromTrain(train: Train): Unit = {
    if (!trains.contains(train)) {
      throw new IllegalArgumentException("Player doesn’t own the train")
    }
    carriages = train.removeCarriage() :: carriages
  }

  def disassembleTrain(train: Train): Unit = {
    if (!trains.contains(train)) {
      throw new IllegalArgumentException("Player doesn’t own the train")
    }
    engines = train.engine :: engines
    carriages = train.carriages ::: carriages
    trains = trains diff List(train)
  }

  def launchTravel(train:Train, to:Town): Unit = {
    //(new Travel(train, World.findPath(train.where, to)))::travels
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

  def update(dt: Double): Unit = {
    travels.foreach{ t:Travel => t.update(dt)}
  }

  def owns(train: Train):Boolean = trains.contains(train)
}
