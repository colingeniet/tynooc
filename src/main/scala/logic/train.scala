package logic.train

import logic.route._
import logic.game._
import logic.town._
import logic.travel._

import collection.mutable.HashMap

/** A class mapping names to objects.
 *
 *  @param T the type of objects in the map.
 */
trait NameMap[T] {
  def models: HashMap[String, T]

  /** Get an element from its name.
   *
   *  @param name the element name.
   *  @throws java.util.NoSuchElementException if no such model exists.
   */
  def apply(name: String): T = this.models.get(name).get
}

class Model(val name: String, val price: Double, val upgrades: List[String])

/** An engine model. */
class EngineModel(
  val weight: Double,
  val power: Double,
  val speed: Double,
  val fuelCapacity: Double,
  val consumption: Double,
  name: String, price: Double, upgrades: List[String])
extends Model(name, price, upgrades)

/** EngineModel companion object.
 *
 *  Gets standard models from their names.
 */
object EngineModel extends NameMap[EngineModel] {
  private var _models: HashMap[String, EngineModel] =
    HashMap(
      "Basic" -> new EngineModel(100, 500, 80, 25, 16, "Basic", 5, List("Advanced")),
      "Advanced" -> new EngineModel(120, 900, 120, 50, 20, "Advanced", 10, List()))

  override def models = _models
}

/** A carriage model. */
class CarriageModel(
  val weight: Double,
  val capacity: Int,
  val comfort: Double,
  name: String, price: Double, upgrades: List[String])
extends Model(name, price, upgrades)

/** CarriageModel companion object.
 *
 *  Get standard models from their names.
 */
object CarriageModel extends NameMap[CarriageModel] {
  private var _models: HashMap[String, CarriageModel] =
    HashMap(
      "Basic" -> new CarriageModel(80, 50, 10, "Basic", 5, List("Advanced")),
      "Advanced" -> new CarriageModel(80, 50, 15, "Advanced", 10, List()))

  override def models = _models
}

/** An engine.
 *
 *  @param _model the engine model.
 */
class Engine(private var _model: EngineModel, var town: Town) {
  var health: Double = 100
  var fuel: Double = model.fuelCapacity
  var train: Option[Train] = None

  def isUsed: Boolean = train.isDefined

  def model: EngineModel = _model
  def model_=(newModel: EngineModel): Unit = {
    _model = newModel
    health = 100
    fuel = model.fuelCapacity
  }

  def this(name: String, town: Town) = this(EngineModel(name), town)

  def speed:Double = model.speed
}

/** A carriages
 *
 *  @param _model the carriage model.
 */
class Carriage(var _model: CarriageModel, var town: Town) {
  var health: Double = 100
  var train: Option[Train] = None
  val placePrice: Double = 1

  def isUsed: Boolean = train.isDefined

  def model: CarriageModel = _model
  def model_=(newModel: CarriageModel): Unit = {
    _model = newModel
    health = 100
  }

  def capacity: Int = model.capacity
  def this(name: String, town: Town) = this(CarriageModel(name), town)

  def comfort:Double = model.comfort
}


class Train (
  var engine: Engine,
  var carriages: List[Carriage],
  var town: Town) {
  var travel: Option[Travel] = None

  def onRoute: Boolean = travel.isDefined

  def weight: Double = {
    carriages.foldLeft[Double](engine.model.weight)(_ + _.model.weight)
  }

  def consumption(distance: Double): Double = engine.model.consumption * distance

  def tooHeavy: Boolean = weight > engine.model.power

  def deteriorate(r:Route): Unit = {
    engine.health = Math.max(0, engine.health - 10)
    carriages.foreach { c => c.health = Math.max(0, c.health - 10) }
  }

  /** Adds a carriage at the end of the train. */
  def addCarriage(c: Carriage): Unit = {
    carriages = c :: carriages
  }

  /** Remove the last carriage of the train and returns it.
   *
   *  @throws NoSuchElementException if train has no carriage.
   */
  def removeCarriage(): Carriage = {
    val last = carriages.head
    carriages = carriages.tail
    last
  }
}
