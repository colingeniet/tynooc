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



class VehicleUnitMenu(vehicle: VehicleUnit) extends VBox(3) { menu =>
  private val upgradeButton: Button = new Button("Upgrade") {
    onAction = (event: ActionEvent) => {
      // when pressing the button, display the list of upgrades
      val selectionList: SelectionList[String] =
        new SelectionList[String](
          vehicle.model.upgrades,
          name => s"${name}(${MoneyFormatter.format(PriceSimulation.upgradePrice(vehicle, name))})",
          name => {
            // upgrade engine upon selection
            vehicle.owner.upgrade(vehicle, name)
            // reset content
            menu.setChildren()
        })

      // display new selection list upon button pressed
      menu.setChildren()
      children.add(selectionList)
    }

    disable <== vehicle.isUsed
  }

  def setChildren(): Unit = {
    children = List(upgradeButton)
  }
}

class VehicleMenu(vehicle: Vehicle) extends VehicleUnitMenu(vehicle) {
  val nameField: TextField = new TextField() {
    text <==> vehicle.name
  }

  override def setChildren(): Unit = {
    super.setChildren()
    children.add(0, nameField)
  }
}


object VehicleUnitMenu {
  def apply(vehicle: VehicleUnit): VehicleUnitMenu = {
    vehicle match {
      case e: Engine => new TrainMenu(e)
      case v: Vehicle => new VehicleMenu(v)
      case _ => new VehicleUnitMenu(vehicle)
    }
  }
}
