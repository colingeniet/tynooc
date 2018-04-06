package logic.facility

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

class PortModel(
  val name: String,
  val price: Double,
  val upgrades: List[String],
  val size: Int,
  val beamClearance: Double)
extends StationModel
