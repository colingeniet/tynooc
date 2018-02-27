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
  statsTrain: Train => Unit,
  statsEngine: Engine => Unit,
  statsCarriage: Carriage => Unit)
extends VBox(3) {
  private var menu: SelectionMenu = new SelectionMenu()
  menu.addMenu("trains", displayTrains())
  menu.addMenu("engines", displayEngines())
  menu.addMenu("carriages", displayCarriages())

  private var sep1: Separator = new Separator()
  private var sep2: Separator = new Separator()

  private var list: Node = new Pane()

  children = List(menu, sep1)

  private def displayTrains(): Unit = {
    list = new TrainList(player.trains, detailTrain)
    children = List(menu, sep1, list)
  }

  private def displayEngines(): Unit = {
    list = new EngineList(player.engines, detailEngine)
    children = List(menu, sep1, list)
  }

  private def displayCarriages(): Unit = {
    list = new CarriageList(player.carriages, statsCarriage)
    children = List(menu, sep1, list)
  }

  private def detailTrain(train: Train): Unit = {
    statsTrain(train)
    var disassembleAll: Button = new Button("Disassemble all")
    disassembleAll.onAction = (event: ActionEvent) => {
      player.disassembleTrain(train)
      displayTrains()
    }

    // no Disassemble Last button if train has no carriage
    if(train.carriages.isEmpty) {
      children = List(menu, sep1, list, sep2, disassembleAll)
    } else {
      var disassembleOne: Button = new Button("Disassemble last")
      disassembleOne.onAction = (event: ActionEvent) => {
        player.removeCarriageFromTrain(train)
        detailTrain(train)
      }
      children = List(menu, sep1, list, sep2, disassembleOne, disassembleAll)
    }
  }

  private def detailEngine(engine: Engine): Unit = {
    statsEngine(engine)
    var createButton: Button = new Button("New train")
    createButton.onAction = (event: ActionEvent) => {
      player.createTrainFromEngine(engine)
      displayEngines()
    }
    children = List(menu, sep1, list, sep2, createButton)
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

class EngineList(
  engines: List[Engine],
  detail: Engine => Unit)
extends ScrollPane {
  private var list: SelectionMenu = new SelectionMenu()
  engines.foreach(engine => list.addMenu(engine.model.name, detail(engine)))

  content = list
}
