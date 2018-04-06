package logic.facility

import logic.model._
import logic.town._


trait FacilityModel extends BuyableModel

trait Facility extends Upgradable[FacilityModel] {
  val town: Town
}

abstract class FacilityFromModel[Model <: FacilityModel](
  model: Model,
  val town: Town)
extends FromBuyableModel[Model](model) with Facility
