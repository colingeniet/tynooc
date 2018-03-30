package gui.scenes.panes.vehicle

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._

import gui.scenes.panes._
import gui.scenes.panes.model._
import logic.vehicle._


class VehicleModelStats(model: VehicleUnitModel)
extends BuyableModelStats(model)

class VehicleModelShortStats(model: VehicleUnitModel)
extends BuyableModelShortStats(model)



class VehicleUnitDetail(vehicle: VehicleUnit)
extends VBox(3) {
  private val name: Label = new Label(vehicle.model.name)

  private val model: VBox = new VehicleModelShortStats(vehicle.model) {
    // don't display name
    override def filter(a: java.lang.reflect.Field): Boolean = {
      a.getName() == "name" || super.filter(a)
    }
  }

  children = List(name, model)
}

object VehicleUnitDetail {
  def apply(vehicle: VehicleUnit): VBox =
    new VehicleUnitDetail(vehicle)
}


class VehicleDetail(vehicle: Vehicle)
extends VBox(3) {
  private val name: Label = new Label {
    text <== vehicle.name
  }
  private val status: Label = new Label {
    text <== createStringBinding(
      () => (if (vehicle.onTravel()) "en route to " else "stored at ") + vehicle.town().name,
      vehicle.onTravel,
      vehicle.town)
  }

  private val model: VehicleUnitDetail = new VehicleUnitDetail(vehicle)

  children = List(name, status, model)
}

object VehicleDetail {
  def apply(vehicle: Vehicle): VBox = {
    vehicle match {
      // trains are special and have special code
      case e: Engine => new TrainDetail(e)
      case _ => new VehicleDetail(vehicle)
    }
  }
}
