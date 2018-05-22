package logic.model

import collection.mutable.HashMap
import scalafx.beans.property._
import java.io._

import logic.company._

/** Generic model trait.
 *
 *  A model of something. */
@SerialVersionUID(0L)
trait Model extends Serializable {
  val name: String
}

/** Model companion object trait. */
@SerialVersionUID(0L)
trait ModelNameMap[T <: Model] extends Serializable {
  def models: HashMap[String, T]

  def apply(name: String): T = this.models(name)
}

/** Something with a model. */
@SerialVersionUID(0L)
trait WithModel[+T <: Model] extends Serializable {
  def model: T
}

/** Implements something based on a model. */
class FromModel[T <: Model](var model: T) extends WithModel[T]


/** Model with basic economic attributes. */
trait BuyableModel extends Model{
  val price: Double
  val upgrades: List[String]
}

/* Corresponding implementations */

trait Upgradable[+T <: BuyableModel] extends WithModel[T] {
  var owner: ObjectProperty[Company]
  def modelNameMap(name: String): T
  def upgradeTo(name: String): Unit
}

abstract class FromBuyableModel[T <: BuyableModel](_model: T)
extends FromModel[T](_model) with Upgradable[T] {
  def upgradeTo(newModel: T): Unit = model = newModel

  def upgradeTo(name: String): Unit = this.upgradeTo(this.modelNameMap(name))
}
