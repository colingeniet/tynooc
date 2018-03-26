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


class VehicleUnitDetail(vehicle: VehicleUnit)
extends VBox(3) {
  private val name: Label = new Label {
    text <== vehicle.name
  }
  private val model: VBox = new VehicleModelShortStats(vehicle.model) {
    // don't display name
    override def filter(a: java.lang.reflect.Field): Boolean = {
      a.getName() == "name" || super.filter(a)
    }
  }

  children = List(name, model)
}
