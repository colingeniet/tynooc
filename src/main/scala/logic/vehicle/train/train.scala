package logic.vehicle.train

import scalafx.beans.property._
import scalafx.beans.binding._
import scalafx.collections._
import scalafx.beans.binding.BindingIncludes._

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
  model: Model,
  town: Town,
  owner: Company)
extends VehicleUnitFromModel[Model](model, town, owner) {
  val train: ObjectProperty[Option[Train]] = ObjectProperty(None)

  val isUsed: BooleanBinding =
    Bindings.createBooleanBinding(
      () => train().isDefined,
      train)
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
  def comfort: Double = model.comfort

  def this(name: String, town: Town, owner: Company) = this(CarriageModel(name), town, owner)
}


class Train (
  val engine: Engine,
  _carriages: List[Carriage],
  _town: Town,
  val owner: Company) extends Vehicle {
  val carriages: ObservableBuffer[Carriage] = ObservableBuffer(_carriages)

  val town: ObjectProperty[Town] = ObjectProperty(_town)

  val name: StringProperty = StringProperty("Train")

  if(engine.isUsed())
    throw new IllegalActionException("Can't create train with already used engine.")
  engine.train() = Some(this)

  override def speed: Double = engine.speed
  override def consumption(distance: Double): Double = engine.model.consumption * distance

  val weight: DoubleProperty = DoubleProperty(0)
  weight <== Bindings.createDoubleBinding(
      () => carriages.foldLeft[Double](engine.model.weight)(_ + _.model.weight),
      carriages)

  val tooHeavy: BooleanBinding =
    jfxBooleanBinding2sfx(weight > engine.model.power)

  override val isAvailable: BooleanBinding =
    jfxBooleanBinding2sfx(
      jfxBooleanBinding2sfx(!tooHeavy) && jfxBooleanBinding2sfx(!onTravel))


  val isEmpty: BooleanBinding =
    Bindings.createBooleanBinding(
      () => carriages.isEmpty(),
      carriages)

  /** Adds a carriage at the end of the train. */
  def addCarriage(c: Carriage): Unit = {
    if(c.isUsed())
      throw new IllegalActionException("Can't add used carriage to a train.")
    if(onTravel())
      throw new IllegalActionException("Can't add carriage to on road train.")
    if (town() != c.town())
      throw new IllegalActionException(s"Can't add ${c.town} stocked carriage to a ${town} stocked train.")
    c.train() = Some(this)
    carriages += c
  }

  /** Remove the last carriage of the train and returns it. */
  def removeCarriage(): Carriage = {
    if(onTravel())
      throw IllegalActionException("Can't remove carriage to on road train.")
    val last = carriages.last
    carriages.remove(carriages.size - 1)
    last.train() = None
    last.town() = town()
    last
  }

  /** Disassemble a train. */
  def disassemble(): Unit = {
    if (onTravel())
      throw new IllegalActionException("Can't disassemble used train.")
    engine.town() = town()
    engine.train() = None
    carriages.foreach{ c =>
      c.town() = town()
      c.train() = None
    }
  }

  def launchTravel(newTravel : Travel): Unit = {
    if (onTravel())
      throw new IllegalActionException("Can't launch travel with used train.")
    if (tooHeavy())
      throw new IllegalActionException("Can't launch travel with too heavy train.")
    travel() = Some(newTravel)
  }
}
