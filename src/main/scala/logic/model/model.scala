package logic.model

import collection.mutable.HashMap


trait Model {
  val name: String
}

trait ModelNameMap[T <: Model] {
  def models: HashMap[String, T]

  def apply(name: String): T = this.models.get(name).get
}

class FromModel[T <: Model](var model: T)


trait BuyableModel extends Model{
  val price: Double
  val upgrades: List[String]
}

class FromBuyableModel[T <: BuyableModel](_model: T)
extends FromModel[T](_model) {
  def upgradeTo(newModel: T): Unit = {
    model = newModel
  }
}
