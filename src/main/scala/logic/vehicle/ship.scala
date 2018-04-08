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

/** A ship model. */
class ShipModel(
  val name: String,
  val price: Double,
  val upgrades: List[String],
  val speed: Double,
  val consumption: Double,
  val beamClearance: Double,
  val capacity: Int,
  val comfort: Double,
  val allowed: HashMap[Good, Double])
extends VehicleModel

/** ShipModel companion object.
 *
 *  Gets standard models from their names.
 */
object ShipModel extends ModelNameMap[ShipModel] {
  private var _models: HashMap[String, ShipModel] = HashMap(
    "basic" -> new ShipModel("basic ship", 1000, List("advanced"), 50, 10, 8, 0, 0, Good.any(200)),
    "advanced" -> new ShipModel("advanced ship", 2000, List(), 60, 15, 10, 0, 0, Good.any(350)))

  override def models: HashMap[String, ShipModel] = _models
}


object Ship {
  def apply(model: ShipModel, company: Company): Ship = {
    new Ship(model, company.fabricTown, company)
  }

  def apply(name: String, company: Company): Ship = {
    apply(ShipModel(name), company)
  }
}

class Ship(
  _model: ShipModel,
  _town: Town,
  _owner: Company)
extends VehicleFromModel[ShipModel](_model, _town, _owner) {
  val name: StringProperty = StringProperty("ship")

  def modelNameMap(modelName: String): ShipModel = ShipModel(modelName)

  def createRooms(travel: Travel): List[Room] = List(new Room(travel, this))
}
