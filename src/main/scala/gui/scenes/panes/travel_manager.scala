package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.event._

import gui.scenes.elements._

import logic.travel._
import logic.vehicle._
import logic.game._
import logic.town._
import logic.company._



class ScriptInfo(script: Script, print: String => Unit) extends VBox(3) {
  script.printErr = print

  private val pause: ToggleButton = new ToggleButton("pause") {
    selected <==> script.paused
    selected.onChange({if(!selected()) script.step()})
  }

  private val repeat: ToggleButton = new ToggleButton("repeat") {
    selected <==> script.repeat
  }


  def instructionText(instr: script.TravelInstruction): String = {
    instr match {
      case script.TravelTo(town) => "go to " + town.name
      case script.Wait(delay) => "wait " + Game.timeToHourString(delay)
    }
  }

  private var list: SelectionList[script.TravelInstruction] =
    new SelectionList[script.TravelInstruction](
      script.instructions,
      instructionText(_),
      _ => ())

  private val travelToButton: Button = new Button("go to") {
    onAction = (event: ActionEvent) => {
      // when pressing the button, display the list of towns
      val selectionList: SelectionList[Town] = new SelectionList[Town](
        Game.world.towns.toList,
        _.name,
        town => {
          script.instructions.add(new script.TravelTo(town))
          children = List(pause, repeat, list, travelToButton, timeField, waitButton, deleteButton, clearButton)
        })
      // display new selection list upon button pressed
      children = List(pause, repeat, list, travelToButton, waitButton, timeField, selectionList, deleteButton, clearButton)
    }
  }

  private val waitButton: Button = new Button("wait") {
    onAction = (event: ActionEvent) => {
      script.instructions.add(new script.Wait(timeField.value()))
    }
  }

  val timeField: Spinner[Double] = new Spinner[Double](0, 1000, 1, 0.5) {
    editable = true
  }

  private val deleteButton: Button = new Button("delete") {
    onAction = (event: ActionEvent) => script.instructions.remove(script.instructions.size - 1)
  }

  private val clearButton: Button = new Button("clear") {
    onAction = (event: ActionEvent) => script.instructions.clear()
  }

  children = List(pause, repeat, list, travelToButton, waitButton, timeField, deleteButton, clearButton)
}


class TravelManager(
  company: Company,
  vehicleDetail: Vehicle => Unit,
  print: String => Unit)
extends VBox(3) {
  val list = new SelectionListDynamic[Script](
    company.travel_scripts,
    _.vehicle.name,
    displayScript(_))

  val sep = new Separator()

  children = List(list)

  private def displayScript(script: Script): Unit = {
    vehicleDetail(script.vehicle)
    children = List(list, sep, new ScriptInfo(script, print))
  }
}
