package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._

import gui.draw._
import logic.player._
import logic.train._

class PlayerInfo(
  player: Player,
  detailTrain: Train => Unit,
  detailEngine: Engine => Unit,
  detailCarriage: Carriage => Unit
) extends DrawableVBox {
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
      new TrainList(player.trains, detailTrain)
    )
  }

  private def displayEngines(): Unit = {
    children = List(
      trainsButton,
      enginesButton,
      carriagesButton,
      sep,
      new EngineList(player.engines, detailEngine)
    )
  }

  private def displayCarriages(): Unit = {
    children = List(
      trainsButton,
      enginesButton,
      carriagesButton,
      sep,
      new CarriageList(player.carriages, detailCarriage)
    )
  }
}

class TrainList(trains: List[Train], detail: Train => Unit)
extends ScrollPane {
  private var group: ToggleGroup = new ToggleGroup()
  // radio button list
  private var list: List[RadioButton] =
    trains.map(train =>
      new RadioButton("train") {  // needs name
        onAction = (event: ActionEvent) => detail(train)
        styleClass.remove("radio-button")
        styleClass.add("link")
      }
    )
  list.foreach(group.toggles.add(_))

  content = new VBox(3) {
    children = list
  }
}

class CarriageList(carriages: List[Carriage], detail: Carriage => Unit)
extends ScrollPane {
  private var group: ToggleGroup = new ToggleGroup()
  // radio button list
  private var list: List[RadioButton] =
    carriages.map(carriage =>
      new RadioButton(carriage.model.name) {
        onAction = (event: ActionEvent) => detail(carriage)
        styleClass.remove("radio-button")
        styleClass.add("link")
      }
    )
  list.foreach(group.toggles.add(_))

  content = new VBox(3) {
    children = list
  }
}

class EngineList(engines: List[Engine], detail: Engine => Unit)
extends ScrollPane {
  private var group: ToggleGroup = new ToggleGroup()
  // radio button list
  private var list: List[RadioButton] =
    engines.map(engine =>
      new RadioButton(engine.model.name) {
        onAction = (event: ActionEvent) => detail(engine)
        styleClass.remove("radio-button")
        styleClass.add("link")
      }
    )
  list.foreach(group.toggles.add(_))

  content = new VBox(3) {
    children = list
  }
}
