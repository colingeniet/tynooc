package gui.scenes.panes.vehicle

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._

import gui.scenes.panes.model._
import logic.vehicle._
import logic.vehicle.train._


class VehicleModelStats(model: VehicleUnitModel)
extends BuyableModelStats(model)

class VehicleModelShortStats(model: VehicleUnitModel)
extends BuyableModelShortStats(model)
