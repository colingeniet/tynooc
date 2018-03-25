package logic.vehicle

import scalafx.beans.property._
import scalafx.beans.binding._
import scalafx.beans.binding.BindingIncludes._

import logic.model._
import logic.company._
import logic.town._
import logic.travel._

import collection.mutable.HashMap


trait VehicleUnit {
  val town: ObjectProperty[Town]
  val owner: Company
  val isUsed: BooleanBinding
  val name: StringProperty
}

abstract class VehicleUnitFromModel[Model <: BuyableModel](
  model: Model,
  _town: Town,
  val owner: Company)
extends FromBuyableModel[Model](model) with VehicleUnit {
  val town: ObjectProperty[Town] = ObjectProperty(_town)
  val name: StringProperty = StringProperty(model.name)

  override def upgradeTo(newModel: Model): Unit = {
    if (this.isUsed()) throw new IllegalArgumentException("Vehicle is in use")
    super.upgradeTo(newModel)
  }
}


trait VehicleModel extends BuyableModel {
  val speed: Double
  val consumption: Double
}

trait Vehicle extends VehicleUnit {
  val travel: ObjectProperty[Option[Travel]] = ObjectProperty(None)

  val onTravel: BooleanBinding =
    Bindings.createBooleanBinding(
      () => travel().isDefined,
      travel)

  val isUsed: BooleanBinding = onTravel

  val isAvailable: BooleanBinding = jfxBooleanBinding2sfx(!onTravel)

  def speed: Double
  def consumption(distance: Double): Double
}

abstract class VehicleFromModel[Model <: VehicleModel](
  model: Model,
  town: Town,
  owner: Company)
extends VehicleUnitFromModel(model, town, owner) with Vehicle {
  def speed: Double = model.speed
  def consumption(distance: Double): Double = model.consumption * distance
}
