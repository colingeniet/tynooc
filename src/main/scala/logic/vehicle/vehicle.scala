package logic.vehicle

import scalafx.beans.property._
import scalafx.beans.binding._
import scalafx.beans.binding.BindingIncludes._

import logic.company._
import logic.town._
import logic.travel._

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
  val town: ObjectProperty[Town]
  val owner: Company
  val isUsed: BooleanBinding
}

abstract class VehicleUnitFromModel[Model <: VehicleUnitModel](
  private var _model: Model,
  _town: Town,
  val owner: Company)
extends VehicleUnit {
  val town: ObjectProperty[Town] = ObjectProperty(_town)

  def model: Model = _model

  def upgradeTo(newModel: Model): Unit = {
    if (isUsed()) throw new IllegalArgumentException("Vehicle is in use")
    _model = newModel
  }
}


trait Vehicle {
  val owner: Company
  val town: ObjectProperty[Town]
  val name: StringProperty
  val travel: ObjectProperty[Option[Travel]] = ObjectProperty(None)

  val onTravel: BooleanBinding =
    Bindings.createBooleanBinding(
      () => travel().isDefined,
      travel)

  val isAvailable: BooleanBinding = jfxBooleanBinding2sfx(!onTravel)

  def speed: Double
  def consumption(distance: Double): Double
}
