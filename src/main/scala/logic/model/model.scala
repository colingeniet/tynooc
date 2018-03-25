package logic.model

import collection.mutable.HashMap


class Model(val name: String)

abstract class ModelNameMap[T <: Model] {
  private var _models: HashMap[String, T] = new HashMap()

  def models: HashMap[String, T] = _models
  def models_=(listModels: List[T]): Unit = {
    listModels.foreach(m => _models(m.name) = m)
  }

  def apply(name: String): T = this.models.get(name).get
}

abstract class FromModel[T <: Model](var model: T)


class BuyableModel(
  name: String,
  val price: Double,
  val upgrades: List[String])
extends Model(name)

abstract class FromBuyableModel[T <: BuyableModel](_model: T)
extends FromModel[T](_model) {
  def upgradeTo(newModel: T): Unit = {
    model = newModel
  }
}
