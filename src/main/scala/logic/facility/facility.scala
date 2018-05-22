package logic.facility

import scalafx.beans.property._

import logic.model._
import logic.town._
import logic.company._


trait FacilityModel extends BuyableModel

trait Facility extends Upgradable[FacilityModel] {
  val town: Town
}

abstract class FacilityFromModel[Model <: FacilityModel](
  _model: Model,
  val town: Town,
  _owner: Company)
extends FromBuyableModel[Model](_model) with Facility {
  @transient var owner: ObjectProperty[Company] = ObjectProperty(_owner)
}
