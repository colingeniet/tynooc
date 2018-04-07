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
    "advanced" -> new TruckModel("advanced truck", 400, List(), 100, 3, 0, 0, Good.any(40)))

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
  model: TruckModel,
  _town: Town,
  owner: Company)
extends VehicleFromModel[TruckModel](model, _town, owner) {
  val name: StringProperty = StringProperty("truck")

  def modelNameMap(modelName: String): TruckModel = TruckModel(modelName)

  val contents: HashMap[Good, DoubleProperty] = HashMap()

  def createRooms(travel: Travel): List[Room] = List()

  override def speed(route: Route): Double = {
    route match {
      case r: Road => r.maximum_speed min super.speed(route)
      case _ => super.speed(route)
    }
  }
}
