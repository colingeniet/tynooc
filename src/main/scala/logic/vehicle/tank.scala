
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

import java.io._

/** A Tank model. */
class TankModel(
  val name: String,
  val price: Double,
  val upgrades: List[String],
  val speed: Double,
  val consumption: Double,
  val capacity: Int,
  val comfort: Double,
  val allowed: HashMap[Good, Double])
extends VehicleModel

/** TankModel companion object.
 *
 *  Gets standard models from their names.
 */
object TankModel extends ModelNameMap[TankModel] {
  private val _models: HashMap[String, TankModel] = HashMap(
    "basic" -> new TankModel("basic tank", 0, List(), 350, 6, 30, 15, Good.any(10))
  )

  override def models: HashMap[String, TankModel] = _models

  def specialTankModel(good: Good, quantity: Double) : TankModel = {
      val h = Good.none
      h(good) = quantity
      return new TankModel("Big Brother Tank", 0, List(), 10, 0, 0, 0, h)
    }
}


object Tank {
  def apply(model: TankModel, company: Company): Tank = {
    new Tank(model, company.fabricTown, company)
  }

  def apply(name: String, company: Company): Tank = {
    apply(TankModel(name), company)
  }
}

class Tank(
  _model: TankModel,
  _town: Town,
  _owner: Company)
extends VehicleFromModel[TankModel](_model, _town, _owner) {
  @transient var name: StringProperty = StringProperty("Tank")
  @transient var travel: ObjectProperty[Option[Travel]] = ObjectProperty(None)
  /* Initialization is done through the initBindings() method */
  @transient var onTravel: BooleanBinding = null
  @transient var isAvailable: BooleanBinding = null
  @transient var isUsed: BooleanBinding = null

  this.initBindings()

  def modelNameMap(modelName: String): TankModel = TankModel(modelName)

  override def launchTravel(to: Town): Travel = {
    assert(!onTravel())

    val from = this.town()
    val distX = from.x - to.x
    val distY = from.y - to.y
    val dist = math.hypot(distX, distY)

    val route = new Airway(from, to, dist)
    val newTravel = new Travel(this, List(route))
    travel() = Some(newTravel)
    newTravel
  }

  /** Create a room for the travel to come. Because this is an airTank it only has one room
  * @param travel The travel the Tank is going to do
  */
  def createRooms(travel: Travel): List[Room] = List(new Room(travel, this))

  @throws(classOf[IOException])
  private def writeObject(stream: ObjectOutputStream): Unit = {
    stream.defaultWriteObject()
    stream.writeObject(this.owner())
    stream.writeObject(this.town())
    stream.writeObject(this.name())
    stream.writeObject(this.travel())
  }

  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  private def readObject(stream: ObjectInputStream): Unit = {
    stream.defaultReadObject()
    this.owner = ObjectProperty(stream.readObject().asInstanceOf[Company])
    this.town = ObjectProperty(stream.readObject().asInstanceOf[Town])
    this.name = StringProperty(stream.readObject().asInstanceOf[String])
    this.travel = ObjectProperty(stream.readObject().asInstanceOf[Option[Travel]])
    this.initBindings()
  }
}
