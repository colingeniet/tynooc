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

  val fee : Double = 5 // You pay 5 euros every time you go in a station that is not yours. The player might be able to modify it in the future

  def accepts(vehicle: Vehicle): Boolean

  /** Credits the owner of a vehicle when it enters a station. Gives money to the owner of the station
  * @param vehicle The vehicle in question.
  */
  def onEnter(vehicle: Vehicle): Unit = {
    owner().credit(fee)
    vehicle.owner().debit(fee)
  }
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
   "airport" -> new AirportModel("airport", 5000, List("big_airport"), 2, 3000),
   "big_airport" -> new AirportModel("big airport", 8000, List(), 4, 6500))

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
