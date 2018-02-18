//Static HashMap
object EngineModel {
  private var models : HashMap[String, EngineModel] =
    HashMap(
      ("Basic", new EngineModel(50,50,70,25,15,List("Advanced"),20))
      ("Advanced", new EngineModel(25,100,140,50,5,List(),5))
    )
}

//Static HashMap
object CarriageModel {
  private var models : HashMap[String, CarriageModel] =
    HashMap(
      ("Basic", new EngineModel(50,50,10))
      ("Advanced", new EngineModel(50,50,15))
    )
}

/** An engine model. */
class EngineModel(
  _weight: Double,
  _power: Double,
  _speed: Double,
  _fuelCapacity: Double,
  _price: Double,
  _upgrades: List[String],
  _consumption: Double) {
    def weight: Double = _weight
    def power: Double = _power
    def speed: Double = _speed
    def fuelCapacity: Double = _fuelCapacity
    def price: Double = _price
    def consumption: Double = _consumption
    def upgrades: List[String] = _upgrades

    def apply(s: String): EngineModel = {
      models(s)
    }
}

/** A carriage model. */
class CarriageModel(
  _weight: Double,
  _capacity: Int,
  _price: Double,
  _upgrades: List[String],
  _comfort: Double) {
    def weight: Double = _weight
    def capacity: Int = _capacity
    def comfort: Double = _comfort
    def upgrades: List[String] = _upgrades
    def price: Double = _price

    def apply(s: String): CarriageModel = {
      models(s)
    }
}

/** An engine.
 *
 *  @param _model the engine model.
 */
class Engine(_model: EngineModel) {
  var health: Double = 100
  var fuel: Double = model.fuelCapacity

  def model: EngineModel = _model

  def apply(s: String): Engine = {
    new Engine(new EngineModel(s))
  }
}

/** A carriages
 *
 *  @param _model the carriage model.
 */
class Carriage(_model: CarriageModel) {
  var health: Double = 100

  def model: CarriageModel = _model

  def apply(s: String): Carriage = {
    new Carriage(new CarriageModel(s))
  }
}


class Train (e: Engine, c: List[Carriage]) {
  var engine: Engine = e
  var carriages: List[Carriage] = c

  def weight: Double = {
    (if (engine == null) 0 else engine.model.weight)
    + carriages.foldLeft[Double](0) { (acc, v) => acc + v.model.weight }
  }
}
