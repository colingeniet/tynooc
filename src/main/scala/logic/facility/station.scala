package logic.facility

import scala.collection.mutable.HashMap

import logic.model._
import logic.town._
import logic.vehicle._
import logic.company._


trait StationModel extends FacilityModel {
  val size: Int
}

trait Station extends Facility {
  def accepts(vehicle: Vehicle): Boolean
}

abstract class StationFromModel[Model <: StationModel] (
  model: Model,
  town: Town,
  _owner: Company)
extends FacilityFromModel[Model](model, town, _owner) with Station




class TrainStationModel(
  val name: String,
  val price: Double,
  val upgrades: List[String],
  val size: Int)
extends StationModel

object TrainStationModel extends ModelNameMap[TrainStationModel] {
 private val _models: HashMap[String, TrainStationModel] = HashMap(
   "train station" -> new TrainStationModel("train station", 5000, List(), 4))

 override def models: HashMap[String, TrainStationModel] = _models
}

class TrainStation(model: TrainStationModel, town: Town, _owner: Company)
extends StationFromModel[TrainStationModel](model, town, _owner) {
  def accepts(vehicle: Vehicle): Boolean = {
    vehicle match {
      case e: Engine => true
      case _ => false
    }
  }

  def modelNameMap(name: String): TrainStationModel = TrainStationModel(name)
}


class AirportModel(
  val name: String,
  val price: Double,
  val upgrades: List[String],
  val size: Int,
  val runwayLength: Double)
extends StationModel

object AirportModel extends ModelNameMap[AirportModel] {
 private val _models: HashMap[String, AirportModel] = HashMap(
   "airport" -> new AirportModel("airport", 5000, List(), 2, 3000))

 override def models: HashMap[String, AirportModel] = _models
}

class Airport(model: AirportModel, town: Town, _owner: Company)
extends StationFromModel[AirportModel](model, town, _owner) {
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
 private val _models: HashMap[String, PortModel] = HashMap(
   "port" -> new PortModel("port", 10000, List(), 4, 15))

 override def models: HashMap[String, PortModel] = _models
}

class Port(model: PortModel, town: Town, _owner: Company)
extends StationFromModel[PortModel](model, town, _owner) {
  def accepts(vehicle: Vehicle): Boolean = {
    vehicle match {
      case s: Ship => s.model.beamClearance <= model.beamClearance
      case _ => false
    }
  }

  def modelNameMap(name: String): PortModel = PortModel(name)
}
