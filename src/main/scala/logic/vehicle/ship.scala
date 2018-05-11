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

/** A ship model. */
class ShipModel(
  val name: String,
  val price: Double,
  val upgrades: List[String],
  val speed: Double,
  val consumption: Double,
  val beamClearance: Double,
  val capacity: Int,
  val comfort: Double,
  val allowed: HashMap[Good, Double])
extends VehicleModel

/** ShipModel companion object.
 *
 *  Gets standard models from their names.
 */
object ShipModel extends ModelNameMap[ShipModel] {
  private var _models: HashMap[String, ShipModel] = HashMap(
    "basic" -> new ShipModel("basic ship", 1000, List("advanced"), 50, 10, 8, 0, 0, Good.any(200)),
    "advanced" -> new ShipModel("advanced ship", 2000, List(), 60, 15, 10, 0, 0, Good.any(350)),
    "cargo" -> new ShipModel("cargo", 2000, List("huge_cargo"), 10, 15, 10, 0, 0, Good.any(750)),
    "huge_cargo" -> new ShipModel("huge cargo", 5000, List(), 12, 15, 10, 0, 0, Good.any(1200)))

  override def models: HashMap[String, ShipModel] = _models
}


object Ship {
  def apply(model: ShipModel, company: Company): Ship = {
    new Ship(model, company.fabricTown, company)
  }

  def apply(name: String, company: Company): Ship = {
    apply(ShipModel(name), company)
  }
}

class Ship(
  _model: ShipModel,
  _town: Town,
  _owner: Company)
extends VehicleFromModel[ShipModel](_model, _town, _owner) {
  @transient var name: StringProperty = StringProperty("ship")
  @transient var travel: ObjectProperty[Option[Travel]] = ObjectProperty(None)
  /* Initialization is done through the initBindings() method */
  @transient var onTravel: BooleanBinding = null
  @transient var isAvailable: BooleanBinding = null
  @transient var isUsed: BooleanBinding = null

  this.initBindings()

  def modelNameMap(modelName: String): ShipModel = ShipModel(modelName)

  /** Create a room for the travel to come. Because this is a ship it only has one room.
  * @param travel The travel the ship is going to do
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
