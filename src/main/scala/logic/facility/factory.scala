package logic.facility

import scala.collection.mutable.HashMap

import logic.model._
import logic.good._
import logic.town._
import logic.game._
import logic.company._



class ProductionCycle(
  val consumes: HashMap[Good, Double],
  val produces: HashMap[Good, Double],
  val cycleTime: Double)

class FactoryModel(
  val name: String,
  val price: Double,
  val upgrades: List[String],
  val productions: List[ProductionCycle])
extends FacilityModel

object FactoryModel extends ModelNameMap[FactoryModel] {
 private val _models: HashMap[String, FactoryModel] = HashMap()

 override def models: HashMap[String, FactoryModel] = _models
}

class Factory(model: FactoryModel, _town: Town, _owner: Company)
extends FacilityFromModel[FactoryModel](model, _town, _owner) {
  def modelNameMap(name: String): FactoryModel = FactoryModel(name)


}
