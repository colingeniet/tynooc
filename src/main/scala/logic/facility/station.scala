package logic.facility

import scala.collection.mutable.HashMap

import logic.model._
import logic.town._
import logic.vehicle._


trait StationModel extends FacilityModel {
  val size: Int
}

trait Station extends Facility {
  def accepts(vehicle: Vehicle): Boolean
}

abstract class StationFromModel[Model <: StationModel] (
  model: Model,
  town: Town)
extends FacilityFromModel[Model](model, town) with Station



class AirportModel(
  val name: String,
  val price: Double,
  val upgrades: List[String],
  val size: Int,
  val runwayLength: Double)
extends StationModel

object AirportModel extends ModelNameMap[AirportModel] {
 private val _models: HashMap[String, AirportModel] = HashMap()

 override def models: HashMap[String, AirportModel] = _models
}

class Airport(model: AirportModel, town: Town)
extends StationFromModel[AirportModel](model, town) {
  def accepts(vehicle: Vehicle): Boolean = {
    vehicle match {
      case p: Plane => p.model.requiredRunway <= model.runwayLength
      case _ => false
    }
  }

  def modelNameMap(name: String): AirportModel = AirportModel(name)
}

class PortModel(
  val name: String,
  val price: Double,
  val upgrades: List[String],
  val size: Int,
  val beamClearance: Double)
extends StationModel

object PortModel extends ModelNameMap[PortModel] {
 private val _models: HashMap[String, PortModel] = HashMap()

 override def models: HashMap[String, PortModel] = _models
}

class Port(model: PortModel, town: Town)
extends StationFromModel[PortModel](model, town) {
  def accepts(vehicle: Vehicle): Boolean = {
    vehicle match {
      case s: Ship => s.model.beamClearance <= model.beamClearance
      case _ => false
    }
  }

  def modelNameMap(name: String): PortModel = PortModel(name)
}
