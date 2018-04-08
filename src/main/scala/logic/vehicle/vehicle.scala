package logic.vehicle

import scalafx.beans.property._
import scalafx.beans.binding._
import scalafx.beans.binding.BindingIncludes._

import logic.model._
import logic.company._
import logic.town._
import logic.travel._
import logic.game._
import logic.good._
import logic.room._
import logic.route._

import collection.mutable.HashMap


final case class IllegalActionException(
  private val message: String = "",
  private val cause: Throwable = None.orNull)
extends Exception(message, cause)


/** Model for a vehicle unit.
 *
 *  A vehicle unit is 'any vehicle which can be bought' :
 *  carriage, engine, truck, ship */
trait VehicleUnitModel extends BuyableModel {
  val capacity: Int
  val comfort: Double
  val allowed: HashMap[Good, Double]
}

trait VehicleUnit extends Upgradable[VehicleUnitModel] {
  val town: ObjectProperty[Town]
  val isUsed: BooleanBinding

  def capacity: Int = model.capacity
  def comfort: Double = model.comfort
  def allowed: HashMap[Good, Double] = model.allowed
}

abstract class VehicleUnitFromModel[Model <: VehicleUnitModel](
  _model: Model,
  _town: Town,
  _owner: Company)
extends FromBuyableModel[Model](_model) with VehicleUnit {
  val town: ObjectProperty[Town] = ObjectProperty(_town)
  val owner: ObjectProperty[Company] = ObjectProperty(_owner)

  override def upgradeTo(newModel: Model): Unit = {
    assert(!this.isUsed())
    super.upgradeTo(newModel)
  }
}


/** Model for a vehicle.
 *
 *  A vehicle (by opposition to a vehicle unit) can move :
 *  a carriage is not. */
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
  def speed(route: Route): Double

  def consumption(distance: Double): Double

  def launchTravel(to: Town): Travel = {
    assert(!this.onTravel())
    val routes = Game.world.findPath(this.town(), to, this)
    assert(routes != None)
    val newTravel = new Travel(this, routes.get)
    this.travel() = Some(newTravel)
    newTravel
  }

  def createRooms(travel: Travel): List[Room]
}

abstract class VehicleFromModel[Model <: VehicleModel](
  _model: Model,
  _town: Town,
  _owner: Company)
extends VehicleUnitFromModel(_model, _town, _owner) with Vehicle {
  val name: StringProperty

  def speed: Double = model.speed
  def speed(route: Route): Double = speed

  def consumption(distance: Double): Double = model.consumption * distance
}
