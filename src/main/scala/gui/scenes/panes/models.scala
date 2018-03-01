package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._

import gui.scenes.elements._
import logic.train._
import logic.player._

/** Display a catalog of engines and carriages.
 *
 *  Engines/Carriages can be previewed and bougth from the catalog.
 *  @param player the player buying new stock.
 *  @param updateStock a callback used to indicate that the player stock
 *   needs to be updated.
 */
class ModelsList(player: Player, updateStock: => Unit) extends VBox(3) {
  // 2 submenus : engines and carriages
  private var typeList: SelectionMenu = new SelectionMenu()
  typeList.addMenu("engines", listEngines)
  typeList.addMenu("carriages", listCarriages)

  private var sep1: Separator = new Separator()

  // each submenu has a list of models
  private var  enginesList: SelectionMenu = new SelectionMenu()
  for ((name, model) <- EngineModel.models) {
    enginesList.addMenu(name, displayEngine(model))
  }
  private var  carriagesList: SelectionMenu = new SelectionMenu()
  for ((name, model) <- CarriageModel.models) {
    carriagesList.addMenu(name, displayCarriage(model))
  }

  private var sep2: Separator = new Separator()

  // buy button
  private var buy: Button = new Button()

  children = List(typeList, sep1)

  /** Displays the list of engines. */
  private def listEngines(): Unit = {
    children = List(typeList, sep1, enginesList, sep2)
  }

  /** Displays a specific engine model. */
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

  /** Displays the list of carriages. */
  private def listCarriages(): Unit = {
    children = List(typeList, sep1, carriagesList, sep2)
  }

  /** Displays a specific carriage model. */
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
