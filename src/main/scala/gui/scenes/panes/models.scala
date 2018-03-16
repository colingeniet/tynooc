package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._

import gui.scenes.elements._
import logic.vehicle._
import logic.vehicle.train._
import logic.company._
import formatter._

/** Display a catalog of engines and carriages.
 *
 *  Engines/Carriages can be previewed and bougth from the catalog.
 *  @param company the company buying new stock.
 *  @param updateStock a callback used to indicate that the company stock
 *   needs to be updated.
 */
class ModelsList(company: Company, updateStock: => Unit) extends VBox(3) {
  // 2 submenus : engines and carriages
  private val typeList: SelectionMenu = new SelectionMenu()
  typeList.addMenu("engines", listEngines)
  typeList.addMenu("carriages", listCarriages)

  private val sep1: Separator = new Separator()

  // each submenu has a list of models
  private val enginesList: SelectionList[EngineModel] =
    new SelectionList[EngineModel](
      EngineModel.models.values.toList,
      _.name,
      displayEngine(_))

  private val carriagesList: SelectionList[CarriageModel] =
    new SelectionList[CarriageModel](
      CarriageModel.models.values.toList,
      _.name,
      displayCarriage(_))

  private val sep2: Separator = new Separator()

  // buy button
  private val buy: Button = new Button()

  children = List(typeList, sep1)

  /** Displays the list of engines. */
  private def listEngines(): Unit = {
    enginesList.deselect()
    children = List(typeList, sep1, enginesList, sep2)
  }

  /** Displays a specific engine model. */
  private def displayEngine(engine: EngineModel): Unit = {
    buy.text = "buy(" + MoneyFormatter.format(engine.price) + ")"
    buy.onAction = (event: ActionEvent) => {
      company.buyEngine(engine.name)
      updateStock
    }
    children = List(
      typeList, sep1, enginesList, sep2, buy,
      new EngineModelStats(engine))
  }

  /** Displays the list of carriages. */
  private def listCarriages(): Unit = {
    carriagesList.deselect()
    children = List(typeList, sep1, carriagesList, sep2)
  }

  /** Displays a specific carriage model. */
  private def displayCarriage(carriage: CarriageModel): Unit = {
    buy.text = "buy(" + MoneyFormatter.format(carriage.price) + ")"
    buy.onAction = (event: ActionEvent) => {
      company.buyCarriage(carriage.name)
      updateStock
    }
    children = List(
      typeList, sep1, carriagesList, sep2, buy,
      new CarriageModelStats(carriage))
  }
}
