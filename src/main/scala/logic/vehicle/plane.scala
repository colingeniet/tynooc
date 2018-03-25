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
  models = List(
    new PlaneModel("Basic", 500, List("Advanced"), 200, 20, 30),
    new PlaneModel("Advanced", 1000, List(), 300, 20, 40))
}

class Plane(
  model: PlaneModel,
  town: Town,
  owner: Company)
extends VehicleFromModel[PlaneModel](model, town, owner)
