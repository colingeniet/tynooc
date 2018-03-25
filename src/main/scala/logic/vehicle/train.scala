package logic.vehicle.train

import scalafx.beans.property._
import scalafx.beans.binding._
import scalafx.collections._
import scalafx.beans.binding.BindingIncludes._

import logic.route._
import logic.town._
import logic.travel._
import logic.company._
import logic.model._
import logic.vehicle._

import collection.mutable.HashMap

final case class IllegalActionException(
  private val message: String = "",
  private val cause: Throwable = None.orNull)
extends Exception(message, cause)


/** Template for a vehicle model class. */
trait RailVehicleUnitModel extends VehicleUnitModel {
  val weight: Double
}

/** An engine model. */
class EngineModel(
  val name: String,
  val price: Double,
  val upgrades: List[String],
  val weight: Double,
  val speed: Double,
  val consumption: Double,
  val power: Double)
extends RailVehicleUnitModel with VehicleModel

/** EngineModel companion object.
 *
 *  Gets standard models from their names.
 */
object EngineModel extends ModelNameMap[EngineModel] {
  models = List(
    new EngineModel("Basic", 500,  List("Advanced"), 100, 80, 10, 500),
    new EngineModel("Advanced", 1000, List(), 120, 120, 12, 900))
}

/** A carriage model. */
class CarriageModel(
  val name: String,
  val price: Double,
  val upgrades: List[String],
  val weight: Double,
  val capacity: Int,
  val comfort: Double)
extends RailVehicleUnitModel

/** CarriageModel companion object.
 *
 *  Get standard models from their names.
 */
object CarriageModel extends ModelNameMap[CarriageModel] {
  models = List(
    new CarriageModel("Basic", 500, List("Advanced"), 80, 40, 10),
    new CarriageModel("Advanced", 1000, List(), 80, 50, 15))
}


/** A carriage.
 *
 *  @param _model the carriage model.
 */
class Carriage(model: CarriageModel, town: Town, owner: Company)
extends VehicleUnitFromModel[CarriageModel](model, town, owner) {
  val train: ObjectProperty[Option[Engine]] = ObjectProperty(None)

  val isUsed: BooleanBinding =
    Bindings.createBooleanBinding(
      () => train().isDefined,
      train)

  var placePrice: Double = 0.20

  def capacity: Int = model.capacity
  def comfort: Double = model.comfort

  def this(name: String, town: Town, owner: Company) =
    this(CarriageModel(name), town, owner)
}

/** An engine.
 *
 *  @param _model the engine model.
 */
class Engine(
  model: EngineModel,
  _town: Town,
  owner: Company,
  _carriages: List[Carriage] = List())
extends VehicleFromModel[EngineModel](model, _town, owner) {
  val carriages: ObservableBuffer[Carriage] = ObservableBuffer(_carriages)

  val weight: DoubleProperty = DoubleProperty(0)
  weight <== Bindings.createDoubleBinding(
      () => carriages.foldLeft[Double](model.weight)(_ + _.model.weight),
      carriages)

  val tooHeavy: BooleanBinding =
    jfxBooleanBinding2sfx(weight > model.power)

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

  def this(name: String, _town: Town, owner: Company, _carriages: List[Carriage]) =
    this(EngineModel(name), _town, owner, _carriages)
}
