package logic.vehicle

import scalafx.beans.property._
import scalafx.beans.binding._
import scalafx.beans.binding.BindingIncludes._

import logic.model._
import logic.company._
import logic.town._
import logic.travel._

import collection.mutable.HashMap


class VehicleUnitModel(
  name: String,
  val price: Double,
  upgrades: List[String])
extends Model(name, upgrades)


trait VehicleUnit {
  val town: ObjectProperty[Town]
  val owner: Company
  val isUsed: BooleanBinding
}

abstract class VehicleUnitFromModel[Model <: VehicleUnitModel](
  model: Model,
  _town: Town,
  val owner: Company)
extends FromModel[Model](model) with VehicleUnit {
  val town: ObjectProperty[Town] = ObjectProperty(_town)

  override def upgradeTo(newModel: Model): Unit = {
    if (isUsed()) throw new IllegalArgumentException("Vehicle is in use")
    super.upgradeTo(newModel)
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
