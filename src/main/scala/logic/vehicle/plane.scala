package logic.vehicle.plane

import logic.route._
import logic.town._
import logic.travel._
import logic.company._
import logic.model._
import logic.vehicle._

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
  val capacity: Int)
extends VehicleModel

/** EngineModel companion object.
 *
 *  Gets standard models from their names.
 */
object PlaneModel extends ModelNameMap[PlaneModel] {
  private var _models: HashMap[String, PlaneModel] = HashMap(
    "basic" -> new PlaneModel("basic plane", 500, List("advanced"), 200, 20, 30),
    "advanced" -> new PlaneModel("advanced plane", 1000, List(), 300, 20, 40))

  override def models: HashMap[String, PlaneModel] = _models
}

abstract class Plane(
  model: PlaneModel,
  _town: Town,
  owner: Company)
extends VehicleFromModel[PlaneModel](model, _town, owner) {
  def modelNameMap(name: String): PlaneModel = PlaneModel(name)
}
