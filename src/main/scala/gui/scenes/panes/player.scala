package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._


import gui.scenes.elements._
import logic.company._
import logic.vehicle._
import logic.world._
import formatter._

/** Company information panel.
 *
 *  Allows to view company's rolling stock, buy new stock,
 *  assemble trains ...
 *  @param company the company.
 *  @param detailTrain callback used to display info on a train in another panel.
 *  @param detailEngine callback used to display info on an engine in another panel.
 *  @param detailCarriage callback used to display info on a carriage in another panel.
 */
class CompanyInfo(
  company: Company,
  world: World,
  detailVehicle: Vehicle => Unit,
  detailVehicleUnit: VehicleUnit => Unit)
extends VBox(5) {
  private val nameField: TextField = new TextField {
      text = company.name
      onAction = (event: ActionEvent) => {
        company.name = text()
        parent.value.requestFocus()
      }
    }
  private val money: Label = new Label {
    text <== createStringBinding(
      () => MoneyFormatter.format(company.money()),
      company.money)
  }
  private val sep1: Separator = new Separator()
  private val menu: SelectionMenu = new SelectionMenu()
  private val sep2: Separator = new Separator()

  menu.addMenu("vehicles", displayVehicles())
  menu.addMenu("stock", displayVehicleUnits())
  menu.addMenu("catalog", displayCatalog())

  children = List(nameField, money, sep1, menu, sep2)

  /** Displays vehicles panel. */
  private def displayVehicles(): Unit = {
    children = List(
      nameField,
      money,
      sep1,
      menu,
      sep2,
      new VehicleList(company, world, detailVehicle))
  }

  /** Displays stock panel. */
  private def displayVehicleUnits(): Unit = {
    children = List(
      nameField,
      money,
      sep1,
      menu,
      sep2,
      new VehicleUnitList(company, world, detailVehicleUnit))
  }

  /** Displays catalog panel. */
  private def displayCatalog(): Unit = {
    children = List(
      nameField,
      money,
      sep1,
      menu,
      sep2,
      new Catalog(company))
  }
}
