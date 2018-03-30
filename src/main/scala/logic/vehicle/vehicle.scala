package logic.vehicle

import scalafx.beans.property._
import scalafx.beans.binding._
import scalafx.beans.binding.BindingIncludes._

import logic.model._
import logic.company._
import logic.town._
import logic.travel._

import collection.mutable.HashMap


trait VehicleUnitModel extends BuyableModel

trait VehicleUnit extends Upgradable[VehicleUnitModel] {
  val town: ObjectProperty[Town]
  val owner: Company
  val isUsed: BooleanBinding
}

abstract class VehicleUnitFromModel[Model <: VehicleUnitModel](
  model: Model,
  _town: Town,
  val owner: Company)
extends FromBuyableModel[Model](model) with VehicleUnit {
  val town: ObjectProperty[Town] = ObjectProperty(_town)

  override def upgradeTo(newModel: Model): Unit = {
    if (this.isUsed()) throw new IllegalArgumentException("Vehicle is in use")
    super.upgradeTo(newModel)
  }
}


trait VehicleModel extends VehicleUnitModel {
  val speed: Double
  val consumption: Double
}

trait Vehicle extends VehicleUnit {
  val name: StringProperty
  val travel: ObjectProperty[Option[Travel]] = ObjectProperty(None)

  val onTravel: BooleanBinding =
    Bindings.createBooleanBinding(
      () => travel().isDefined,
      travel)

  val isUsed: BooleanBinding = onTravel

  val isAvailable: BooleanBinding = jfxBooleanBinding2sfx(!onTravel)

  def speed: Double
  def consumption(distance: Double): Double

  def launchTravel(destination: Town): Travel
}

abstract class VehicleFromModel[Model <: VehicleModel](
  model: Model,
  town: Town,
  owner: Company)
extends VehicleUnitFromModel(model, town, owner) with Vehicle {
  val name: StringProperty = StringProperty("train")
  def speed: Double = model.speed
  def consumption(distance: Double): Double = model.consumption * distance
}
