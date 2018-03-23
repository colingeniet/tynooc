package logic.plane

import logic.route._
import logic.town._
import logic.travel._
import logic.company._
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
object PlaneModel extends NameMap[PlaneModel] {
  private var _models: HashMap[String, PlaneModel] =
    HashMap(
      "Basic" -> new PlaneModel("Basic", 5, 50, 10, 50, 100, List("Advanced")),
      "Advanced" -> new PlaneModel("Advanced", 10, 20, 500, 100, 200, List()))

  override def models = _models
}

class Plane(
  _name: String,
  _model: PlaneModel,
  town: Town,
  owner: Company) extends VehicleUnitFromModel[PlaneModel](_model, town, owner) with Vehicle {

  var name: String = _name
  def speed: Double = _model.speed
  def consumption: Double = _model.consumption

  def isUsed: Boolean = true
}
