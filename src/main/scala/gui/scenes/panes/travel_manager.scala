package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.event._

import gui.scenes.elements._
import formatter._
import logic.travel._
import logic.vehicle._
import logic.game._
import logic.town._
import logic.company._
import logic.good._



class ScriptInfo(script: Script) extends VBox(3) {
  private val pause: ToggleButton = new ToggleButton {
    text <== createStringBinding(
      () => if (script.started()) "pause" else "start",
      script.started)

    selected <==> script.started
    selected.onChange({if(selected()) script.start()})
  }

  private val repeat: ToggleButton = new ToggleButton("repeat") {
    selected <==> script.repeat
  }


  def instructionText(instr: script.TravelInstruction): String = {
    instr match {
      case script.TravelTo(town) => s"go to ${town.name}"
      case script.Wait(delay) => s"wait ${TimeFormatter.timeToHourString(delay)}"
      case script.Buy(g, q) => s"buy ${q} ${g.name}"
      case script.Sell(g, q) => s"sell ${q} ${g.name}"
    }
  }

  private var list: SelectionList[script.TravelInstruction] =
    new SelectionList[script.TravelInstruction](
      script.instructions,
      instructionText(_),
      _ => ())

  private val travelToButton: Button = new Button("go to") {
    onAction = (event: ActionEvent) => {
      val v = script.vehicle
      // when pressing the button, display the list of towns
      val selectionList: SelectionList[Town] = new SelectionList[Town](
        Game.world.townsAccessibleFrom(v.town(), v),
        _.name,
        town => {
          script.instructions.add(new script.TravelTo(town))
          setChildren()
        })
      // display new selection list upon button pressed
      setChildren()
      children.add(selectionList)
    }
  }

  private def goodSelectionList(f: Good => Unit) : SelectionList[Good] = new SelectionList[Good](
    Good.all,
    _.name,
    f)

    private val buyButton: Button = new Button("buy") {
      onAction = (event: ActionEvent) => {

        setChildren()

        children.add(goodSelectionList(g => {
          script.instructions.add(new script.Buy(g, timeField.value()))
          setChildren()
        }))
      }
    }

    private val sellButton: Button = new Button("buy") {
      onAction = (event: ActionEvent) => {

        setChildren()

        children.add(goodSelectionList(g => {
          script.instructions.add(new script.Sell(g, timeField.value()))
          setChildren()
        }))
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

    disable = script.instructions.isEmpty
    script.instructions.onChange({ disable = script.instructions.isEmpty })
  }

  private val clearButton: Button = new Button("clear") {
    onAction = (event: ActionEvent) => script.instructions.clear()
  }

  private def setChildren(): Unit = {
    children = List(pause, repeat, list, travelToButton, buyButton, sellButton, waitButton, timeField, deleteButton, clearButton)
  }

  setChildren()
}


class TravelManager(
  company: Company,
  vehicleDetail: Vehicle => Unit)
extends VBox(3) {
  val list = new SelectionListDynamic[Script](
    company.travelScripts,
    _.vehicle.name,
    displayScript(_))

  val sep = new Separator()

  children = List(list)

  private def displayScript(script: Script): Unit = {
    vehicleDetail(script.vehicle)
    children = List(list, sep, new ScriptInfo(script))
  }
}
