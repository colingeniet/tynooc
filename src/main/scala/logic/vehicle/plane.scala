package logic.plane

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
  name: String,
  val power: Double,
  val consumption: Double,
  val speed: Double,
  val capacity: Int,
  price: Double,
  upgrades: List[String])
extends VehicleUnitModel(name, price, upgrades)

/** EngineModel companion object.
 *
 *  Gets standard models from their names.
 */
object PlaneModel extends ModelNameMap[PlaneModel] {
  models = List(
    new PlaneModel("Basic", 5, 50, 10, 50, 100, List("Advanced")),
    new PlaneModel("Advanced", 10, 20, 500, 100, 200, List()))
}

class Plane(
  _name: String,
  _model: PlaneModel,
  town: Town,
  owner: Company) extends VehicleUnitFromModel[PlaneModel](_model, town, owner) with Vehicle {

  val name: StringProperty = StringProperty(_name)
  def speed: Double = _model.speed
  def consumption(distance: Double): Double = _model.consumption * distance

  val isUsed: BooleanBinding = Bindings.createBooleanBinding(() => true)
}
