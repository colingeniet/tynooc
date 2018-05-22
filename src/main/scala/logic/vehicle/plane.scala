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

/** A plane model. */
class PlaneModel(
  val name: String,
  val price: Double,
  val upgrades: List[String],
  val speed: Double,
  val consumption: Double,
  val requiredRunway: Double,
  val capacity: Int,
  val comfort: Double,
  val allowed: HashMap[Good, Double])
extends VehicleModel

/** PlaneModel companion object.
 *
 *  Gets standard models from their names.
 */
object PlaneModel extends ModelNameMap[PlaneModel] {
  private val _models: HashMap[String, PlaneModel] = HashMap(
    "basic" -> new PlaneModel("basic plane", 500, List("advanced"), 350, 6, 1500, 30, 15, Good.any(10)),
    "advanced" -> new PlaneModel("advanced plane", 1000, List(), 500, 6, 2000, 40, 20, Good.any(20)),
    "fast_supply_plane" -> new PlaneModel("fast supply plane", 800, List(), 650, 6, 1500, 30, 15, Good.anyWith[CityNeeded](50)))

  override def models: HashMap[String, PlaneModel] = _models
}


object Plane {
  def apply(model: PlaneModel, company: Company): Plane = {
    new Plane(model, company.fabricTown, company)
  }

  def apply(name: String, company: Company): Plane = {
    apply(PlaneModel(name), company)
  }
}

class Plane(
  _model: PlaneModel,
  _town: Town,
  _owner: Company)
extends VehicleFromModel[PlaneModel](_model, _town, _owner) {
  @transient var name: StringProperty = StringProperty("plane")
  @transient var travel: ObjectProperty[Option[Travel]] = ObjectProperty(None)
  /* Initialization is done through the initBindings() method */
  @transient var onTravel: BooleanBinding = null
  @transient var isAvailable: BooleanBinding = null
  @transient var isUsed: BooleanBinding = null

  this.initBindings()

  def modelNameMap(modelName: String): PlaneModel = PlaneModel(modelName)

  override def launchTravel(to: Town): Travel = {
    assert(!onTravel())

    val from = this.town()
    val distX = from.x - to.x
    val distY = from.y - to.y
    val dist = math.hypot(distX, distY)

    val route = new Airway(from, to, dist)
    val newTravel = new Travel(this, List(route))
    travel() = Some(newTravel)
    newTravel
  }

  /** Create a room for the travel to come. Because this is an airplane it only has one room
  * @param travel The travel the plane is going to do
  */
  def createRooms(travel: Travel): List[Room] = List(new Room(travel, this))

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
