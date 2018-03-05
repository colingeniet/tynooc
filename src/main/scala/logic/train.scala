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
   *  @throws NoSuchElementException if no such model exists.
   */
  def apply(name: String): T = this.models.get(name).get
}

/** Template for a vehicle model class. */
class VehicleModel(
  val name: String,
  val weight: Double,
  val health: Double,
  val price: Double,
  val upgrades: List[String])

/** An engine model. */
class EngineModel(
  name: String,
  weight: Double,
  val power: Double,
  val speed: Double,
  val consumption: Double,
  health: Double,
  price: Double,
  upgrades: List[String])
extends VehicleModel(name, weight, health, price, upgrades)

/** EngineModel companion object.
 *
 *  Gets standard models from their names.
 */
object EngineModel extends NameMap[EngineModel] {
  private var _models: HashMap[String, EngineModel] =
    HashMap(
      "Basic" -> new EngineModel("Basic", 100, 500, 80, 10, 100, 500, List("Advanced")),
      "Advanced" -> new EngineModel("Advanced", 120, 900, 120, 12, 100, 1000, List()))

  override def models = _models
}

/** A carriage model. */
class CarriageModel(
  name: String,
  weight: Double,
  val capacity: Int,
  val comfort: Double,
  health: Double,
  price: Double,
  upgrades: List[String])
extends VehicleModel(name, weight, health, price, upgrades)

/** CarriageModel companion object.
 *
 *  Get standard models from their names.
 */
object CarriageModel extends NameMap[CarriageModel] {
  private var _models: HashMap[String, CarriageModel] =
    HashMap(
      "Basic" -> new CarriageModel("Basic", 80, 40, 10, 100, 500, List("Advanced")),
      "Advanced" -> new CarriageModel("Advanced", 80, 60, 15, 100, 1000, List()))

  override def models = _models
}

trait Vehicle {
  var train: Option[Train]
  var health: Double

  def isUsed: Boolean = train.isDefined

  def repairPrice: Double
  def repair(): Unit
}

/** Implements [[Vehicle]] based on a [[VehicleModel]]. */
class VehicleFromModel[Model <: VehicleModel](
  private var _model: Model,
  var town: Town)
extends Vehicle {
  var train: Option[Train] = None
  var health: Double = model.health

  def damaged: Boolean = health == 0

  def model: Model = _model
  def model_=(newModel: Model): Unit = {
    _model = newModel
    repair()
  }

  override def repairPrice: Double = 0.25 * model.price * (health/model.health)
  override def repair(): Unit = health = model.health
}

/** An engine.
 *
 *  @param _model the engine model.
 */
class Engine(model: EngineModel, town: Town)
extends VehicleFromModel[EngineModel](model, town) {
  def this(name: String, town: Town) = this(EngineModel(name), town)

  def speed:Double = model.speed * (0.75 + 0.25 * health/model.health)
}

/** A carriage.
 *
 *  @param _model the carriage model.
 */
class Carriage(model: CarriageModel, town: Town)
extends VehicleFromModel[CarriageModel](model, town) {
  var placePrice: Double = 0.20

  def capacity: Int = model.capacity
  def comfort:Double = model.comfort * (0.75 + 0.25 * health/model.health)

  def this(name: String, town: Town) = this(CarriageModel(name), town)
}


class Train (
  var engine: Engine,
  var carriages: List[Carriage],
  var town: Town) {
  var name: String = "train"

  var travel: Option[Travel] = None

  def onRoute: Boolean = travel.isDefined

  def damaged: Boolean = engine.damaged || carriages.exists(_.damaged)

  def weight: Double = {
    carriages.foldLeft[Double](engine.model.weight)(_ + _.model.weight)
  }

  def consumption(distance: Double): Double = engine.model.consumption * distance

  def tooHeavy: Boolean = weight > engine.model.power

  def deteriorate(route:Route): Unit = {
    engine.health = Math.max(0, engine.health - route.damageToVehicle)
    carriages.foreach { c => c.health = Math.max(0, c.health - route.damageToVehicle) }
  }

  def isAvailable: Boolean = !damaged && !onRoute && !tooHeavy

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
