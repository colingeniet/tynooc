package gui.scenes.panes.vehicle

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._

import gui.scenes.panes._
import gui.scenes.panes.model._
import gui.scenes.elements._
import formatter._
import logic.vehicle._
import logic.company._


class VehicleModelStats(model: VehicleUnitModel)
extends BuyableModelStats(model) {
  override def filter(a: java.lang.reflect.Field): Boolean = {
    a.getName() == "allowed" || super.filter(a)
  }
}

class VehicleModelShortStats(model: VehicleUnitModel)
extends BuyableModelShortStats(model) {
  override def filter(a: java.lang.reflect.Field): Boolean = {
    a.getName() == "allowed" || super.filter(a)
  }
}



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

object VehicleUnitDetail {
  def apply(vehicle: VehicleUnit): VBox = {
    vehicle match {
      // trains are special and have special code
      case e: Engine => new TrainDetail(e)
      case v: Vehicle => new VehicleDetail(v)
      case _ => new VehicleUnitDetail(vehicle)
    }
  }
}



class VehicleUnitMenu(vehicle: VehicleUnit, company: Company)
extends UpgradeMenu[VehicleUnitModel](vehicle, company) {
  upgradeButton.disable <== vehicle.isUsed || vehicle.owner =!= company
}

class VehicleMenu(vehicle: Vehicle, company: Company)
extends VehicleUnitMenu(vehicle, company) {
  private val nameField: TextField = new TextField() {
    text <==> vehicle.name
  }

  override def setChildren(): Unit = {
    super.setChildren()
    children.add(0, nameField)
  }
}


object VehicleUnitMenu {
  def apply(vehicle: VehicleUnit, company: Company): VBox = {
    val menu = vehicle match {
      case e: Engine => new TrainMenu(e, company)
      case v: Vehicle => new VehicleMenu(v, company)
      case _ => new VehicleUnitMenu(vehicle, company)
    }
    menu.setChildren()
    menu
  }
}
