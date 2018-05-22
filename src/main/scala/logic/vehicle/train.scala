package logic.vehicle

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
import logic.world._
import logic.game._
import logic.good._
import logic.room._

import collection.mutable.HashMap
import java.io._


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
extends RailVehicleUnitModel with VehicleModel {
  val capacity: Int = 0
  val comfort: Double = 0
  val allowed: HashMap[Good, Double] = Good.none
}

/** EngineModel companion object.
 *
 *  Gets standard models from their names.
 */
object EngineModel extends ModelNameMap[EngineModel] {
  private var _models: HashMap[String, EngineModel] = HashMap(
    "basic" -> new EngineModel("basic engine", 500,  List("advanced"), 100, 80, 10, 500),
    "advanced" -> new EngineModel("advanced engine", 1000, List(), 120, 120, 12, 900))
  override def models: HashMap[String, EngineModel] = _models
}

/** A carriage model. */
class CarriageModel(
  val name: String,
  val price: Double,
  val upgrades: List[String],
  val weight: Double,
  val capacity: Int,
  val comfort: Double,
  val allowed: HashMap[Good, Double])
extends RailVehicleUnitModel

/** CarriageModel companion object.
 *
 *  Get standard models from their names.
 */
object CarriageModel extends ModelNameMap[CarriageModel] {
  private var _models: HashMap[String, CarriageModel] = HashMap(
    "basic" -> new CarriageModel("basic carriage", 500, List("advanced"), 80, 40, 10, Good.any(10)),
    "advanced" -> new CarriageModel("advanced carriage", 1000, List(), 80, 50, 15, Good.any(20)),
    "liquid" -> new CarriageModel("liquid specialized carriage", 650, List(), 80, 50, 15, Good.anyWith[Liquid](40)),
    "standard" -> new CarriageModel("standard carriage", 700, List(), 80, 50, 15, Good.anyWith[CityNeeded](40))
  )

  override def models: HashMap[String, CarriageModel] = _models
}


object Carriage {
  def apply(model: CarriageModel, company: Company): Carriage = {
    new Carriage(model, company.fabricTown, company)
  }

  def apply(name: String, company: Company): Carriage = {
    apply(CarriageModel(name), company)
  }
}

/** A carriage.
 *
 *  @param _model the carriage model.
 */
class Carriage(_model: CarriageModel, _town: Town, _owner: Company)
extends VehicleUnitFromModel[CarriageModel](_model, _town, _owner) {
  @transient var train: ObjectProperty[Option[Engine]] = ObjectProperty(None)
  @transient var isUsed: BooleanBinding = Bindings.createBooleanBinding(
    () => train().isDefined,
    train)

  def modelNameMap(name: String): CarriageModel = CarriageModel(name)

  @throws(classOf[IOException])
  private def writeObject(stream: ObjectOutputStream): Unit = {
    stream.defaultWriteObject()
    stream.writeObject(this.owner())
    stream.writeObject(this.town())
    stream.writeObject(this.train())
  }

  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  private def readObject(stream: ObjectInputStream): Unit = {
    stream.defaultReadObject()
    this.owner = ObjectProperty(stream.readObject().asInstanceOf[Company])
    this.town = ObjectProperty(stream.readObject().asInstanceOf[Town])
    this.train = ObjectProperty(stream.readObject().asInstanceOf[Option[Engine]])
    this.isUsed = Bindings.createBooleanBinding(
      () => train().isDefined,
      train)
  }
}




object Engine {
  def apply(model: EngineModel, company: Company): Engine = {
    new Engine(model, company.fabricTown, company)
  }

  def apply(name: String, company: Company): Engine = {
    apply(EngineModel(name), company)
  }
}

/** An engine.
 *
 *  @param _model the engine model.
 */
class Engine(
  _model: EngineModel,
  _town: Town,
  _owner: Company,
  _carriages: List[Carriage] = List())
extends VehicleFromModel[EngineModel](_model, _town, _owner) {
  @transient var name: StringProperty = StringProperty("train")
  @transient var travel: ObjectProperty[Option[Travel]] = ObjectProperty(None)
  @transient var carriages: ObservableBuffer[Carriage] = ObservableBuffer(_carriages)
  /* Initialization is done through the initBindings() method */
  @transient var onTravel: BooleanBinding = null
  @transient var isAvailable: BooleanBinding = null
  @transient var isUsed: BooleanBinding = null
  @transient var weight: NumberBinding = null
  @transient var tooHeavy: BooleanBinding = null
  @transient var isEmpty: BooleanBinding = null

  override protected def initBindings(): Unit = {
    super.initBindings()
    this.weight = Bindings.createDoubleBinding(
      () => carriages.foldLeft[Double](model.weight)(_ + _.model.weight),
      carriages)
    this.tooHeavy = jfxBooleanBinding2sfx(weight > model.power)
    this.isAvailable = jfxBooleanBinding2sfx(
      jfxBooleanBinding2sfx(!tooHeavy) && jfxBooleanBinding2sfx(!onTravel))
    this.isEmpty = Bindings.createBooleanBinding(
      () => carriages.isEmpty(),
      carriages)
  }

  this.initBindings()

  /** Adds a carriage at the end of the train.
  * @param c The cariage to add
  */
  def addCarriage(c: Carriage): Unit = {
    assert(!c.isUsed() && !onTravel() && town() == c.town())
    c.train() = Some(this)
    carriages += c
  }

  /** Remove the last carriage of the train and returns it. */
  def removeCarriage(): Carriage = {
    assert(!onTravel())
    val last = carriages.last
    carriages.remove(carriages.size - 1)
    last.train() = None
    last.town() = town()
    last
  }

  /** Disassemble a train. */
  def disassemble(): Unit = {
    assert(!onTravel())

    carriages.foreach{ c =>
      c.town() = town()
      c.train() = None
    }
    carriages.clear()
  }

  override def launchTravel(to: Town): Travel = {
    assert(!this.tooHeavy())
    super.launchTravel(to)
  }

  /** Returns the maximum speed over a certain route
  * @param route The route you're on
  */
  override def speed(route: Route): Double = {
    route match {
      case r: Rail => r.maximum_speed min super.speed(route)
      case _ => super.speed(route)
    }
  }

  /** Create a room for the travel to come. Because this is an train it has a room for each carriage it has !
  * @param travel The travel the train is going to do
  */
  def createRooms(travel: Travel): List[Room] =
    carriages.toList.map(new Room(travel, _))

  def modelNameMap(name: String): EngineModel = EngineModel(name)

  /** Upgrades the vehicle to one of it's upgrades
  * @param newModel The model you want to upgrade to
  */
  override def upgradeTo(newModel: EngineModel): Unit = {
    super.upgradeTo(newModel)

    weight = Bindings.createDoubleBinding(
        () => carriages.foldLeft[Double](model.weight)(_ + _.model.weight),
        carriages)

    tooHeavy = jfxBooleanBinding2sfx(weight > model.power)
  }

  @throws(classOf[IOException])
  private def writeObject(stream: ObjectOutputStream): Unit = {
    stream.defaultWriteObject()
    stream.writeObject(this.owner())
    stream.writeObject(this.town())
    stream.writeObject(this.carriages.toList)
    stream.writeObject(this.name())
    stream.writeObject(this.travel())
  }

  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  private def readObject(stream: ObjectInputStream): Unit = {
    stream.defaultReadObject()
    this.owner = ObjectProperty(stream.readObject().asInstanceOf[Company])
    this.town = ObjectProperty(stream.readObject().asInstanceOf[Town])
    this.carriages = ObservableBuffer[Carriage](stream.readObject().asInstanceOf[List[Carriage]])
    this.name = StringProperty(stream.readObject().asInstanceOf[String])
    this.travel = ObjectProperty(stream.readObject().asInstanceOf[Option[Travel]])
    this.initBindings()
  }
}
