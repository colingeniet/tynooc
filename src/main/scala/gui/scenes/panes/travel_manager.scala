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



class ScriptInfo(script: Script, vehicleDetail: Vehicle => Unit) extends VBox {
  private val vehicle: Button = new Button {
    text <== script.vehicle.name
    onAction = (event: ActionEvent) => vehicleDetail(script.vehicle)
    styleClass.remove("button")
    styleClass.add("link")
  }

  private val pause: ToggleButton = new ToggleButton("pause") {
    selected <==> script.paused
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

  children = list
}
