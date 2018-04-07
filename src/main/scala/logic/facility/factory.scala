package logic.facility

import scala.collection.mutable.HashMap

import logic.model._
import logic.good._
import logic.town._
import logic.game._
import logic.company._


class FactoryModel(
  val name: String,
  val price: Double,
  val upgrades: List[String],
  val consumes: HashMap[Good, Double],
  val produces: HashMap[Good, Double],
  val cycleTime: Double)
extends FacilityModel

object FactoryModel extends ModelNameMap[FactoryModel] {
 private val _models: HashMap[String, FactoryModel] = HashMap()

 override def models: HashMap[String, FactoryModel] = _models
}

class Factory(model: FactoryModel, _town: Town, _owner: Company)
extends FacilityFromModel[FactoryModel](model, _town, _owner) {
  def modelNameMap(name: String): FactoryModel = FactoryModel(name)

  def startCycle(): Unit = {
    if(town.consume(model.consumes))
    Game.delayAction(model.cycleTime, () => produce())
  }

  private def produce(): Unit = {
    model.produces.foreach{ case (g,v) =>
      town.addGoods(g, v)
    }
  }
}
