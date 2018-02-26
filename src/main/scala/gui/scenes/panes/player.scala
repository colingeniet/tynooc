package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._

import gui.draw._

class PlayerInfo extends DrawableVBox {
  private var listGroup: ToggleGroup = new ToggleGroup

  private var trainsButton: RadioButton = new RadioButton("trains") {
    onAction = (event: ActionEvent) => displayTrains()
    styleClass.remove("radio-button")
    styleClass.add("link")
  }
  listGroup.toggles.add(trainsButton)

  private var enginesButton: RadioButton = new RadioButton("engines") {
    onAction = (event: ActionEvent) => displayEngines()
    styleClass.remove("radio-button")
    styleClass.add("link")
  }
  listGroup.toggles.add(enginesButton)

  private var carriagesButton: RadioButton = new RadioButton("carriages") {
    onAction = (event: ActionEvent) => displayCarriages()
    styleClass.remove("radio-button")
    styleClass.add("link")
  }
  listGroup.toggles.add(carriagesButton)

  private var sep: Separator = new Separator()
  trainsButton.fire()

  private def displayTrains(): Unit = {
    children = List(
      trainsButton,
      enginesButton,
      carriagesButton,
      sep,
      new TrainList()
    )
  }

  private def displayEngines(): Unit = {
    children = List(
      trainsButton,
      enginesButton,
      carriagesButton,
      sep,
      new EngineList()
    )
  }

  private def displayCarriages(): Unit = {
    children = List(
      trainsButton,
      enginesButton,
      carriagesButton,
      sep,
      new CarriageList()
    )
  }
}

class TrainList extends DrawableVBox {
  children = new Label("Train list")
}

class CarriageList extends DrawableVBox {
  children = new Label("Carriage list")
}

class EngineList extends DrawableVBox {
  children = new Label("Engine list")
}
