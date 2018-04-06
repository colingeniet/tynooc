package logic.model

import collection.mutable.HashMap

/** Generic model trait.
 *
 *  A model of something. */
trait Model {
  val name: String
}

/** Model companion object trait. */
trait ModelNameMap[T <: Model] {
  def models: HashMap[String, T]

  def apply(name: String): T = this.models.get(name).get
}

/** Something with a model. */
trait WithModel[+T <: Model] {
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
  def modelNameMap(name: String): T
  def upgradeTo(name: String): Unit
}

abstract class FromBuyableModel[T <: BuyableModel](_model: T)
extends FromModel[T](_model) with Upgradable[T] {
  def upgradeTo(newModel: T): Unit = model = newModel

  def upgradeTo(name: String): Unit = this.upgradeTo(this.modelNameMap(name))
}
