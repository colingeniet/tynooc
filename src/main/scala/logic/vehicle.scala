package logic.vehicle

import logic.company._
import logic.town._
import logic.travel._
import logic.train._

import collection.mutable.HashMap


/** A class mapping names to objects.
 *
 *  @param T the type of objects in the map.
 */
trait NameMap[T] {
  def models: HashMap[String, T]

  /** Get an element from its name.
   *
   *  @param name the element name.
   */
  def apply(name: String): T = this.models.get(name).get
}

class VehicleUnitModel(
  val name: String,
  val price: Double,
  val upgrades: List[String])


trait VehicleUnit {
  var town: Town
  val owner: Company
  def isUsed: Boolean
}

abstract class VehicleUnitFromModel[Model <: VehicleUnitModel](
  private var _model: Model,
  var town: Town,
  val owner: Company)
extends VehicleUnit {
  def model: Model = _model

  def upgradeTo(newModel: Model): Unit = {
    if (isUsed) throw new IllegalArgumentException("Vehicle is in use")
    _model = newModel
  }
}


trait Vehicle {
  val owner: Company
  var town: Town
  var name: String
  var travel: Option[Travel] = None

  def onTravel: Boolean = travel.isDefined
  def isAvailable: Boolean = !onTravel

  def speed: Double
  def consumption: Double
}
