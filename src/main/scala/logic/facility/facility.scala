package logic.facility

import logic.model._
import logic.town._
import logic.company._


trait FacilityModel extends BuyableModel

trait Facility extends Upgradable[FacilityModel] {
  val town: Town
  var owner: Company
}

abstract class FacilityFromModel[Model <: FacilityModel](
  model: Model,
  val town: Town,
  var owner: Company)
extends FromBuyableModel[Model](model) with Facility
