package logic.facility

import scala.collection.mutable.HashMap

import logic.model._
import logic.good._
import logic.town._
import logic.game._


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

class Factory(model: FactoryModel, _town: Town)
extends FacilityFromModel[FactoryModel](model, _town) {
  def modelNameMap(name: String): FactoryModel = FactoryModel(name)

  def startCycle(): Unit = {
    this.consume()
    Game.delayAction(model.cycleTime, () => produce())
  }

  private def consume(): Unit = {

  }

  private def produce(): Unit = {

  }
}
