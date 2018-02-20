package player

import world._
import train._


class Travel(t: Train, l : List[World.Route]) {
  /* time and percent, redundant
   */
  val train: Train = t
  val stops: List[World.Route] = l
  var time: Double = 0 //Time since beginning of journey
  var percent: Double = 0 //What percent has been done on the road i
  private var current: Int = 0 //The road you're taking

  def timeRemaining: Double = {
    var d = stops.takeRight(stops.length - current + 1).foldLeft[Double](0) {
      (acc, r) => acc + r.length
    }
    d += stops(current).length * percent
    d / (t.engine.model.speed)
  }

  def next: World.Town = {
    stops(current).destination
  }
}

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

  def buyEngine(name: String): Unit = {
    var c = EngineModel(name)
    if (c.price >= money) {
      this.money -= c.price
      engines = (new Engine(c))::engines
    }
  }

  def buyCarriage(name: String): Unit = {
    var c = CarriageModel(name)
    if (c.price >= money) {
      this.money -= c.price
      carriages = (new Carriage(c))::carriages
    }
  }

  def assembleTrain(e: Engine, c: List[Carriage]): Unit = {
    engines = engines diff List(e)
    carriages = carriages diff List(c)
    trains = (new Train(e, c))::trains
  }

  def launchTravel(train:Train, to:World.Town): Unit = {
    //(new Travel(train, World.Findpath(train.where, to)))::travels
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
}
