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

/** An exception which could be throwed if a player try to launchTravel
  * a travel to an unattainable destination.
  */
final case class PathNotFoundException(
  private val message: String = "",
  private val cause: Throwable = None.orNull)
extends Exception(message, cause)


/** Model for a vehicle unit.
 *
 *  A vehicle unit is 'any vehicle which can be bought' :
 *  carriage, engine, truck, ship */
trait VehicleUnitModel extends BuyableModel {
  val allowed: HashMap[Good, Double]
}

trait VehicleUnit extends Upgradable[VehicleUnitModel] {
  val town: ObjectProperty[Town]
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
  var owner: Company)
extends FromBuyableModel[Model](model) with VehicleUnit {
  val town: ObjectProperty[Town] = ObjectProperty(_town)

  override def upgradeTo(newModel: Model): Unit = {
    if (this.isUsed()) throw new IllegalArgumentException("Vehicle is in use")
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
    if (this.onTravel())
      throw new IllegalActionException("Can't launch travel with used vehicle.")

    val routes = Game.world.findPath(this.town(), to, this).getOrElse(
      throw new PathNotFoundException(s"No usable route to ${to.name}."))
    val newTravel = new Travel(this, routes)
    this.travel() = Some(newTravel)
    newTravel
  }

  def createRooms(travel: Travel): List[Room]
}

abstract class VehicleFromModel[Model <: VehicleModel](
  model: Model,
  town: Town,
  owner: Company)
extends VehicleUnitFromModel(model, town, owner) with Vehicle {
  val name: StringProperty

  def speed: Double = model.speed
  def speed(route: Route): Double = speed

  def consumption(distance: Double): Double = model.consumption * distance
}
