package logic.vehicle

import logic.route._
import logic.town._
import logic.travel._
import logic.company._
import logic.model._
import logic.vehicle._
import logic.good._
import logic.room._

import collection.mutable.HashMap
import scalafx.beans.binding._
import scalafx.beans.property._

import java.io._

/** A truck model. */
class TruckModel(
  val name: String,
  val price: Double,
  val upgrades: List[String],
  val speed: Double,
  val consumption: Double,
  val capacity: Int,
  val comfort: Double,
  val allowed: HashMap[Good, Double])
extends VehicleModel

/** TruckModel companion object.
 *
 *  Gets standard models from their names.
 */
object TruckModel extends ModelNameMap[TruckModel] {
  private var _models: HashMap[String, TruckModel] = HashMap(
    "basic" -> new TruckModel("basic truck", 200, List("advanced"), 80, 3, 0, 0, Good.any(30)),
    "advanced" -> new TruckModel("advanced truck", 400, List(), 100, 3, 0, 0, Good.any(40)),
    "jeep" -> new TruckModel("jeep", 250, List(), 750, 3, 0, 0, Good.any(10)),
    "cheap truck" -> new TruckModel("cheap truck", 50, List(), 180, 3, 0, 0, Good.any(5)))

  override def models: HashMap[String, TruckModel] = _models
}


object Truck {
  def apply(model: TruckModel, company: Company): Truck = {
    new Truck(model, company.fabricTown, company)
  }

  def apply(name: String, company: Company): Truck = {
    apply(TruckModel(name), company)
  }
}

class Truck(
  _model: TruckModel,
  _town: Town,
  _owner: Company)
extends VehicleFromModel[TruckModel](_model, _town, _owner) {
  @transient var name: StringProperty = StringProperty("truck")
  @transient var travel: ObjectProperty[Option[Travel]] = ObjectProperty(None)
  /* Initialization is done through the initBindings() method */
  @transient var onTravel: BooleanBinding = null
  @transient var isAvailable: BooleanBinding = null
  @transient var isUsed: BooleanBinding = null

  this.initBindings()

  def modelNameMap(modelName: String): TruckModel = TruckModel(modelName)

  /** Create a room for the travel to come. Because this is a truck it only has one room
  * @param travel The travel the truck is going to do
  */
  def createRooms(travel: Travel): List[Room] = List(new Room(travel, this))

  /** Gives the max speed of the truck on the road 'road'
  * @param road the road the truck is on
  */
  override def speed(road: Route): Double = {
    road match {
      case r: Road => r.maximum_speed min super.speed(road)
      case _ => super.speed(road)
    }
  }

  @throws(classOf[IOException])
  private def writeObject(stream: ObjectOutputStream): Unit = {
    stream.defaultWriteObject()
    stream.writeObject(this.owner())
    stream.writeObject(this.town())
    stream.writeObject(this.name())
    stream.writeObject(this.travel())
  }

  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  private def readObject(stream: ObjectInputStream): Unit = {
    stream.defaultReadObject()
    this.owner = ObjectProperty(stream.readObject().asInstanceOf[Company])
    this.town = ObjectProperty(stream.readObject().asInstanceOf[Town])
    this.name = StringProperty(stream.readObject().asInstanceOf[String])
    this.travel = ObjectProperty(stream.readObject().asInstanceOf[Option[Travel]])
    this.initBindings()
  }
}
