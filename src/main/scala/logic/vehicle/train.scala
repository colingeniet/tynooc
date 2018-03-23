package logic.train

import logic.route._
import logic.town._
import logic.travel._
import logic.company._
import logic.vehicle._

import collection.mutable.HashMap

final case class IllegalActionException(
  private val message: String = "",
  private val cause: Throwable = None.orNull)
extends Exception(message, cause)

/** A class mapping names to objects.
 *
 *  @param T the type of objects in the map.
 */
trait NameMap[T] {
  def models: HashMap[String, T]

  /** Get an element from its name.
   *
   *  @param name the element name.
   */
  def apply(name: String): T = this.models.get(name).get
}

/** Template for a vehicle model class. */
class RailVehicleModel(
  name: String,
  val weight: Double,
  price: Double,
  upgrades: List[String])
extends VehicleUnitModel(name, price, upgrades)

/** An engine model. */
class EngineModel(
  name: String,
  weight: Double,
  val power: Double,
  val speed: Double,
  val consumption: Double,
  price: Double,
  upgrades: List[String])
extends RailVehicleModel(name, weight, price, upgrades)

/** EngineModel companion object.
 *
 *  Gets standard models from their names.
 */
object EngineModel extends NameMap[EngineModel] {
  private var _models: HashMap[String, EngineModel] =
    HashMap(
      "Basic" -> new EngineModel("Basic", 100, 500, 80, 10, 500, List("Advanced")),
      "Advanced" -> new EngineModel("Advanced", 120, 900, 120, 12, 1000, List()))

  override def models = _models
}

/** A carriage model. */
class CarriageModel(
  name: String,
  weight: Double,
  val capacity: Int,
  val comfort: Double,
  price: Double,
  upgrades: List[String])
extends RailVehicleModel(name, weight,  price, upgrades)

/** CarriageModel companion object.
 *
 *  Get standard models from their names.
 */
object CarriageModel extends NameMap[CarriageModel] {
  private var _models: HashMap[String, CarriageModel] =
    HashMap(
      "Basic" -> new CarriageModel("Basic", 80, 40, 10, 500, List("Advanced")),
      "Advanced" -> new CarriageModel("Advanced", 80, 60, 15, 1000, List()))

  override def models = _models
}


class RailVehicle[Model <: RailVehicleModel](
  _model: Model,
  town: Town,
  owner: Company)
extends VehicleUnitFromModel[Model](_model, town, owner) {
  var train: Option[Train] = None

  override def isUsed: Boolean = train.isDefined
}

/** An engine.
 *
 *  @param _model the engine model.
 */
class Engine(model: EngineModel, town: Town, owner: Company)
extends RailVehicle[EngineModel](model, town, owner) {
  def this(name: String, town: Town, owner: Company) = this(EngineModel(name), town, owner)

  def speed:Double = model.speed
}

/** A carriage.
 *
 *  @param _model the carriage model.
 */
class Carriage(model: CarriageModel, town: Town, owner: Company)
extends RailVehicle[CarriageModel](model, town, owner) {
  var placePrice: Double = 0.20

  def capacity: Int = model.capacity
  def comfort:Double = model.comfort

  def this(name: String, town: Town, owner: Company) = this(CarriageModel(name), town, owner)
}


class Train (
  var engine: Engine,
  var carriages: List[Carriage],
  var town: Town,
  var owner: Company) {

  if(engine.isUsed)
    throw new IllegalActionException("Can't create train with already used engine.")
  engine.train = Some(this)

  var name: String = "Train name"

  var travel: Option[Travel] = None

  def onRoute: Boolean = travel.isDefined

  def weight: Double = {
    carriages.foldLeft[Double](engine.model.weight)(_ + _.model.weight)
  }

  def consumption(distance: Double): Double = engine.model.consumption * distance

  def tooHeavy: Boolean = weight > engine.model.power

  def isAvailable: Boolean = !onRoute && !tooHeavy

  /** Adds a carriage at the end of the train. */
  def addCarriage(c: Carriage): Unit = {
    if(c.isUsed)
      throw new IllegalActionException("Can't add used carriage to a train.")
    if(onRoute)
      throw new IllegalActionException("Can't add carriage to on road train.")
    if (town != c.town)
      throw new IllegalActionException(s"Can't add ${c.town} stocked carriage to a ${town} stocked train.")
    c.train = Some(this)
    carriages = c :: carriages
  }

  /** Remove the last carriage of the train and returns it. */
  def removeCarriage(): Carriage = {
    if(onRoute)
      throw IllegalActionException("Can't remove carriage to on road train.")
    val last = carriages.head
    carriages = carriages.tail
    last.train = None
    last.town = town
    last
  }

  /** Disassemble a train. */
  def disassemble(): Unit = {
    if (onRoute)
      throw new IllegalActionException("Can't disassemble used train.")
    engine.town = town
    engine.train = None
    carriages.foreach{ c =>
      c.town = town
      c.train = None
    }
  }

  def launchTravel(newTravel : Travel): Unit = {
    if (onRoute)
      throw new IllegalActionException("Can't launch travel with used train.")
    if (tooHeavy)
      throw new IllegalActionException("Can't launch travel with too heavy train.")
    travel = Some(newTravel)
  }
}
