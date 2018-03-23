package logic.model

import collection.mutable.HashMap

class Model(val name: String, val upgrades: List[String])

abstract class ModelNameMap[T <: Model] {
  private var _models = HashMap[String, T] = new HashMap()

  def models: HashMap[String, T] = _models
  def models_=(listModels: List(T)): Unit = {
    listModels.foreach(m => _models.add(m.name, m))
  }

  def apply(name: String): T = this.models.get(name).get
}

abstract class FromModel[T <: Model](private var _model: T) {
  def model: T = _model

  def upgradeTo(newModel: T): Unit = {
    _model = newModel
  }
}
