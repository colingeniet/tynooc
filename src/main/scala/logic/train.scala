package logic.train

import logic.world._
import collection.mutable.HashMap

/** A class mapping names to objects.
 *
 *  @param T the type of objects in the map.
 */
abstract class NameMap[T] {
  private var models: HashMap[String, T] = HashMap()

  /** Get an element from its name.
   *
   *  @param name the element name.
   *  @throw java.util.NoSuchElementException if no such model exists.
   */
  def apply(name: String): T = models.get(name).get
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
}

/** EngineModel companion object.
 *
 *  Gets standard models from their names.
 */
object EngineModel extends NameMap[EngineModel] {
  private var models: HashMap[String, EngineModel] =
    HashMap(
      "Basic" -> new EngineModel(50, 50, 70, 25, 15, List("Advanced"), 20),
      "Advanced" -> new EngineModel(25, 100, 140, 50, 5, List(), 5)
    )
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
}

/** CarriageModel companion object.
 *
 *  Get standard models from their names.
 */
object CarriageModel extends NameMap[CarriageModel] {
  private var models: HashMap[String, CarriageModel] =
    HashMap(
      "Basic" -> new CarriageModel(50, 50, 10, List("Advanced"), 1),
      "Advanced" -> new CarriageModel(50, 50, 15, List(), 5)
    )
}

/** An engine.
 *
 *  @param mod the engine model.
 */
class Engine(mod: EngineModel) {
  var _model: EngineModel = mod
  var health: Double = 100
  var fuel: Double = model.fuelCapacity
  var used: Boolean = false

  def model: EngineModel = _model
  def model_=(newModel: EngineModel): Unit = {
    _model = newModel
    health = 100
    fuel = model.fuelCapacity
  }

  def this(name: String) = this(EngineModel(name))
}

/** A carriages
 *
 *  @param mod the carriage model.
 */
class Carriage(mod: CarriageModel) {
  var _model: CarriageModel = mod
  var health: Double = 100
  var used: Boolean = false

  def model: CarriageModel = _model
  def model_=(newModel: CarriageModel): Unit = {
    _model = newModel
    health = 100
  }

  def this(name: String) = this(CarriageModel(name))
}


class Train (e: Engine, c: List[Carriage]) {
  var engine: Engine = e
  var carriages: List[Carriage] = c
  var onRoad: Boolean = false

  def weight: Double = {
    (if (engine == null) 0 else engine.model.weight)
    + carriages.foldLeft[Double](0) { (acc, v) => acc + v.model.weight }
  }

  def deteriorate(r:World.Route): Unit = {
    engine.health -= Math.max(0, engine.health - 10)
    carriages.foreach { c => c.health = Math.max(0, c.health - 10) }
  }
}
