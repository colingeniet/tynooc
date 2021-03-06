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
  var town: ObjectProperty[Town]
  var isUsed: BooleanBinding

  def capacity: Int = model.capacity
  def comfort: Double = model.comfort
  def allowed: HashMap[Good, Double] = model.allowed
}

abstract class VehicleUnitFromModel[Model <: VehicleUnitModel](
  _model: Model,
  _town: Town,
  _owner: Company)
extends FromBuyableModel[Model](_model) with VehicleUnit {
  @transient var town: ObjectProperty[Town] = ObjectProperty(_town)
  @transient var owner: ObjectProperty[Company]  = ObjectProperty(_owner)
  @transient var isUsed: BooleanBinding

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
  var name: StringProperty
  var travel: ObjectProperty[Option[Travel]]

  var onTravel: BooleanBinding
  var isAvailable: BooleanBinding

  protected def initBindings(): Unit = {
    this.onTravel = Bindings.createBooleanBinding(
      () => this.travel().isDefined,
      this.travel)
    this.isUsed = this.onTravel
    this.isAvailable = jfxBooleanBinding2sfx(!this.onTravel)
  }

  def speed: Double
  def speed(route: Route): Double

  def consumption(distance: Double): Double

  /** Starts a new travel to a certain town
  * @param to The town you want to go to
  */
  def launchTravel(to: Town): Travel = {
    assert(!this.onTravel())
    val routes = Game.world.findPath(this.town(), to, this)
    assert(routes != None)
    val newTravel = new Travel(this, routes.get)
    this.travel() = Some(newTravel)
    newTravel
  }

  /** Used to create rooms at the beginning of a travel
  * @param travel The travel you're going to do
  */
  def createRooms(travel: Travel): List[Room]
}

abstract class VehicleFromModel[Model <: VehicleModel](
  _model: Model,
  _town: Town,
  _owner: Company)
extends VehicleUnitFromModel(_model, _town, _owner) with Vehicle {
  def speed: Double = model.speed
  def speed(route: Route): Double = speed

  def consumption(distance: Double): Double = model.consumption * distance
}
