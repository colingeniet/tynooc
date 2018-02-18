class Travel(t: Train, l : List[Route]) {
  /* time and percent, redundant
   */
  val train: Train = t
  val stops: List[Route] = l
  val time: Double = 0 //Time since beginning of journey
  val percent: Double = 0 //What percent has been done on the road i
  private val current: Int //The road you're taking

  def timeRemaining: Double = {
    var d = stops.takeRight(stops.length-i+1).foldLeft[Double](0) { (acc, r) => acc + r.length }
    d += stops(i).length*percent
    0 max d/(t.engine.model.speed)
  }

  def next: Town = {
    stops(i).destination
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


  var money: Double

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
    c.foreach { carriages = carriages diff List(c)}
    trains = (new Train(e, c))::trains
  }

  def launchTravel(train:Train, to:Town): Unit = {
    (new Travel(train, World.Findpath(train.where, to)))::travels
  }

  def editEngine(old: Engine, model: EngineModel): Unit = {
    if (money >= PriceSimulation.upgradePrice(old, model)) {
      old = new Engine(model)
      money -= PriceSimulation.upgradePrice(old, model)
    }
  }
  /**
  I DONT KNOW IF old ACTS LIKE A POINTER IF NOT THIS IS INVALID
  **/
  def editCarriage(old: Carriage, model: CarriageModel): Unit = {
    if (money >= PriceSimulation.upgradePrice(old, model)) {
      old = new Carriage(model)
      money -= PriceSimulation.upgradePrice(old, model)
    }
  }
}
