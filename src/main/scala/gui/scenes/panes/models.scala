package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._

import gui.scenes.elements._
import logic.train._
import logic.player._

class ModelsList(player: Player, updateStock: => Unit) extends VBox(3) {
  private var typeList: SelectionMenu = new SelectionMenu()
  typeList.addMenu("engines", listEngines)
  typeList.addMenu("carriages", listCarriages)

  private var sep1: Separator = new Separator()

  private var  enginesList: SelectionMenu = new SelectionMenu()
  for ((name, model) <- EngineModel.models) {
    enginesList.addMenu(name, displayEngine(model))
  }
  private var  carriagesList: SelectionMenu = new SelectionMenu()
  for ((name, model) <- CarriageModel.models) {
    carriagesList.addMenu(name, displayCarriage(model))
  }

  private var sep2: Separator = new Separator()

  private var buy: Button = new Button()

  children = List(typeList, sep1)

  private def listEngines(): Unit = {
    children = List(typeList, sep1, enginesList, sep2)
  }

  private def displayEngine(engine: EngineModel): Unit = {
    buy.text = "buy(" + engine.price + ")"
    buy.onAction = (event: ActionEvent) => {
      player.buyEngine(engine.name)
      updateStock
    }
    children = List(
      typeList, sep1, enginesList, sep2, buy,
      new EngineModelStats(engine))
  }

  private def listCarriages(): Unit = {
    children = List(typeList, sep1, carriagesList, sep2)
  }

  private def displayCarriage(carriage: CarriageModel): Unit = {
    buy.text = "buy(" + carriage.price + ")"
    buy.onAction = (event: ActionEvent) => {
      player.buyCarriage(carriage.name)
      updateStock
    }
    children = List(
      typeList, sep1, carriagesList, sep2, buy,
      new CarriageModelStats(carriage))
  }
}
