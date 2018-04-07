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
    "basic" -> new PlaneModel("basic plane", 500, List("advanced"), 350, 6, 1500, 30, 15, Good.any(30)),
    "advanced" -> new PlaneModel("advanced plane", 1000, List(), 500, 6, 2000, 40, 20, Good.any(40)))

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
  model: PlaneModel,
  _town: Town,
  owner: Company)
extends VehicleFromModel[PlaneModel](model, _town, owner) {
  val name: StringProperty = StringProperty("plane")

  def modelNameMap(modelName: String): PlaneModel = PlaneModel(modelName)

  def capacity: Int = model.capacity
  def comfort: Double = model.comfort

  val contents: HashMap[Good, DoubleProperty] = HashMap()

  override def launchTravel(to: Town): Travel = {
    if (onTravel())
      throw new IllegalActionException("Can't launch travel with used vehicle.")

    val from = this.town()
    val distX = from.x - to.x
    val distY = from.y - to.y
    val dist = math.hypot(distX, distY)

    val route = new Airway(from, to, dist)
    val newTravel = new Travel(this, List(route))
    travel() = Some(newTravel)
    newTravel
  }

  def createRooms(travel: Travel): List[Room] = {
    List(new Room(travel, this))
  }
}
