package logic.vehicle

import scalafx.beans.property._
import scalafx.beans.binding._
import scalafx.beans.binding.BindingIncludes._

import logic.model._
import logic.company._
import logic.town._
import logic.travel._

import logic.good._
import logic.room._

import collection.mutable.HashMap

final case class IllegalActionException(
  private val message: String = "",
  private val cause: Throwable = None.orNull)
extends Exception(message, cause)


trait VehicleUnitModel extends BuyableModel {

  val allowed: HashMap[Good, Double]
}

trait VehicleUnit extends Upgradable[VehicleUnitModel] {
  val town: ObjectProperty[Town]
  val owner: Company
  val isUsed: BooleanBinding

  val contents: HashMap[Good, Double]

  def load(g: Good, i: Int) : Unit = {

    if (model.allowed(g) < contents(g) + i)
      throw new IllegalActionException("Can't load that much on your unit !")

    contents(g) += i
  }

  def handleGoods(dt: Double) : Unit = {

    contents.foreach{ case (key, value) => if (value > 0) key.update(this, dt) }
  }
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
  def createRooms(travel: Travel): List[Room]
}

abstract class VehicleFromModel[Model <: VehicleModel](
  model: Model,
  town: Town,
  owner: Company)
extends VehicleUnitFromModel(model, town, owner) with Vehicle {
  val name: StringProperty
  def speed: Double = model.speed
  def consumption(distance: Double): Double = model.consumption * distance
}
