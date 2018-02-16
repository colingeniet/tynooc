/** An engine model. */
class EngineModel(
  _weight: Double,
  _power: Double,
  _speed: Double,
  _fuelCapacity: Double,
  _consumption: Double) {
    def weight: Double = _weight
    def power: Double = _power
    def speed: Double = _speed
    def fuelCapacity: Double = _fuelCapacity
    def consumption: Double = _consumption
}

/** A carriage model. */
class CarriageModel(
  _weight: Double,
  _capacity: Int,
  _confort: Double) {
    def weight: Double = _weight
    def capacity: Int = _capacity
    def confort: Double = _confort
}

/** An engine.
 *
 *  @param _model the engine model.
 */
class Engine(_model: EngineModel) {
  var health: Double = 100
  var fuel: Double = model.fuelCapacity

  def model: EngineModel = _model
}

/** A carriages
 *
 *  @param _model the carriage model.
 */
class Carriage(_model: CarriageModel) {
  var health: Double = 100

  def model: CarriageModel = _model
}


class Train() {
  var engine: Engine = null
  var carriages: List[Carriage] = List()

  def weight: Double = {
    (if (engine == null) 0 else engine.model.weight)
    + carriages.foldLeft[Int](0, _+(_.model.weight))
  }
}
