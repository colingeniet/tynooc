package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._
import scalafx.scene.input.MouseEvent

import gui.scenes.elements._
import logic.company._
import logic.vehicle._
import logic.world._
import logic.town._
import formatter._

import scala.util.Try

/** Displays a company rolling stock.
 *
 *  @param company the company.
 *  @param statsTrain a callback used to display information on a train.
 *  @param statsEngine a callback used to display information on a engine.
 *  @param statsCarriage a callback used to display information on a carriage.
 */
class VehicleUnitList(
  company: Company,
  world: World,
  stats: VehicleUnit => Unit)
extends VBox(3) {
  private var list: Node = new Pane()

  private val sep: Separator = new Separator()

  updateList()
  children = List(list)


  private def updateList(): Unit = {
    list = new SelectionList[VehicleUnit](
      company.vehicleUnits.toList,
      _.model.name,
      detailVehicle)
  }

  /** Displays a specific engine. */
  private def detailVehicle(vehicle: VehicleUnit): Unit = {
    // display stats in a separate window via callback
    stats(vehicle)

    val upgradeButton: Button = new Button("Upgrade")
    upgradeButton.onAction = (event: ActionEvent) => {
      // when pressing the button, display the list of upgrades
      val selectionList: SelectionList[String] =
        new SelectionList[String](
          vehicle.model.upgrades,
          name => s"${name}(${MoneyFormatter.format(PriceSimulation.upgradePrice(vehicle, name))})",
          name => {
            // upgrade engine upon selection
            company.upgrade(vehicle, name)
            updateList()
            detailVehicle(vehicle)
        })

      // display new selection list upon button pressed
      children = List(
        list,
        sep,
        upgradeButton,
        new Separator(),
        new Label("select upgrade"),
        selectionList)
    }

    children = List(
      list,
      sep,
      upgradeButton)

    // disable buttons as needed
    upgradeButton.disable <== vehicle.isUsed
  }
}



class VehicleList(
  company: Company,
  world: World,
  stats: Vehicle => Unit)
extends VBox(3) {
  private var list: Node = new Pane()

  private val sep: Separator = new Separator()

  updateList()
  children = List(list)


  private def updateList(): Unit = {
    list = new SelectionListDynamic[Vehicle](
      company.vehicles.toList,
      _.name,
      _ match {
        case e: Engine => detailTrain(e)
        case v: Vehicle => detailVehicle(v)
      })
  }


  /** Displays a specific vehicle. */
  private def detailVehicle(vehicle: Vehicle): Unit = {
    // display stats in a separate window via callback
    stats(vehicle)

    val sendTravel: Button = new Button("Travel")
    val nameField: TextField = new TextField() {
      text <==> vehicle.name
    }

    sendTravel.onAction = (event: ActionEvent) => {
      // when pressing the button, display the list of towns
      val selectionList: SelectionList[Town] = new SelectionList[Town](
        world.townsAccessibleFrom(vehicle.town()),
        _.name,
        town => {
          // when selecting a town, travel to it
          company.launchTravel(vehicle, town)
          detailVehicle(vehicle)
        })
      // display new selection list upon button pressed
      children = List(
        list,
        sep,
        sendTravel,
        nameField,
        new Separator(),
        new Label("select destination"),
        selectionList)
    }

    sendTravel.disable <== vehicle.onTravel

    children = List(
      list,
      sep,
      sendTravel,
      nameField)
  }

  /** Displays a specific train. */
  private def detailTrain(train: Engine): Unit = {
    // display stats in a separate window via callback
    stats(train)

    // create buttons for assemble/disassemble actions
    val disassembleAll: Button = new Button("Disassemble all")
    val disassembleOne: Button = new Button("Disassemble last")
    val addCarriage: Button = new Button("Add carriage")
    val sendTravel: Button = new Button("Travel")
    val nameField: TextField = new TextField() {
      text <==> train.name
    }

    disassembleAll.onAction = (event: ActionEvent) => {
      company.disassembleTrain(train)
    }

    disassembleOne.onAction = (event: ActionEvent) => {
      company.removeCarriageFromTrain(train)
    }

    addCarriage.onAction = (event: ActionEvent) => {
      // when pressing the button, display a new carriage list
      val selectionList: SelectionList[Carriage] =
        new SelectionList(
          company.carriagesStoredAt(train.town()).toList,
          _.model.name,
          carriage => {
            // when selecting a carriage, add it to the train
            company.addCarriageToTrain(train, carriage)
            detailTrain(train)
          })
      // display new selection list upon button pressed
      children = List(
        list,
        sep,
        addCarriage,
        disassembleOne,
        disassembleAll,
        sendTravel,
        nameField,
        new Separator(),
        new Label("select carriage"),
        selectionList)
    }

    sendTravel.onAction = (event: ActionEvent) => {
      // when pressing the button, display the list of towns
      val selectionList: SelectionList[Town] = new SelectionList[Town](
        world.townsAccessibleFrom(train.town()),
        _.name,
        town => {
          // when selecting a town, travel to it
          company.launchTravel(train, town)
          detailTrain(train)
        })
      // display new selection list upon button pressed
      children = List(
        list,
        sep,
        addCarriage,
        disassembleOne,
        disassembleAll,
        sendTravel,
        nameField,
        new Separator(),
        new Label("select destination"),
        selectionList)
    }

    // disable buttons as needed
    addCarriage.disable <== train.onTravel
    disassembleOne.disable <== train.onTravel || train.isEmpty
    disassembleAll.disable <== train.onTravel
    sendTravel.disable <== train.onTravel || train.tooHeavy

    children = List(
      list,
      sep,
      addCarriage,
      disassembleOne,
      disassembleAll,
      sendTravel,
      nameField)
  }
}
