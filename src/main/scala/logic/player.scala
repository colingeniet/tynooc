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

  def assembleTrain(e: Engine, c: List[Carriage]): Unit = {
    engines = engines diff List(e)
    carriages = carriages diff c
    trains = (new Train(e, c))::trains
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
