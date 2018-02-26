package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._

import gui.scenes.elements._
import logic.player._
import logic.train._

class PlayerStock(
  player: Player,
  detailTrain: Train => Unit,
  detailEngine: Engine => Unit,
  detailCarriage: Carriage => Unit)
extends VBox {
  private var menu: SelectionMenu = new SelectionMenu()
  menu.addMenu("trains", displayTrains())
  menu.addMenu("engines", displayEngines())
  menu.addMenu("carriages", displayCarriages())

  private var sep: Separator = new Separator()

  private var list: Node = new Pane()

  setChildren()

  private def displayTrains(): Unit = {
    list = new TrainList(player.trains, detailTrain)
    setChildren()
  }

  private def displayEngines(): Unit = {
    list = new EngineList(player.engines, detailEngine)
    setChildren()
  }

  private def displayCarriages(): Unit = {
    list = new CarriageList(player.carriages, detailCarriage)
    setChildren()
  }

  private def setChildren(): Unit = {
    children = List(menu, sep, list)
  }
}

class TrainList(trains: List[Train], detail: Train => Unit)
extends ScrollPane {
  private var list: SelectionMenu = new SelectionMenu()
  trains.foreach(train => list.addMenu("train", detail(train))) // needs name

  content = list
}

class CarriageList(carriages: List[Carriage], detail: Carriage => Unit)
extends ScrollPane {
  private var list: SelectionMenu = new SelectionMenu()
  carriages.foreach(carriage =>
    list.addMenu(carriage.model.name, detail(carriage)))

  content = list
}

class EngineList(engines: List[Engine], detail: Engine => Unit)
extends ScrollPane {
  private var list: SelectionMenu = new SelectionMenu()
  engines.foreach(engine => list.addMenu(engine.model.name, detail(engine)))

  content = list
}
