package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._



import gui.draw._
import gui.scenes.elements._
import logic.company._
import logic.train._
import logic.world._
import formatter._

/** Company information panel.
 *
 *  Allows to view company's rolling stock, buy new stock,
 *  assemble trains ...
 *  @param company the company.
 *  @param detailTrain callback used to display info on a train in another panel.
 *  @param detailEngine callback used to display info on an engine in another panel.
 *  @param detailCarriage callback used to display info on a carriage in another panel.
 */
class CompanyInfo(
  company: Company,
  world: World,
  detailTrain: Train => Unit,
  detailEngine: Engine => Unit,
  detailCarriage: Carriage => Unit)
extends DrawableVBox {
  private val money: Label = new Label()
  private val sep1: Separator = new Separator()
  private val menu: SelectionMenu = new SelectionMenu()
  private val sep2: Separator = new Separator()
  private var panel: Node = new Pane()
  private val nameField: TextField = new TextField() {
      text = company.name
      onAction = (event: ActionEvent) => {
        company.name = text()
        parent.value.requestFocus()
      }
    }

  menu.addMenu("rolling stock", displayStock())
  menu.addMenu("catalog", displayModels())

  // stock subpanel
  private var stock: CompanyStock =
    new CompanyStock(company, world, detailTrain, detailEngine, detailCarriage)

  // model catalog subpanel
  private val models: ModelsList = new ModelsList(company, updateStock)

  spacing = 5
  draw()
  setChildren()

  /* Updates children list from attributes. */
  private def setChildren(): Unit = {
    children = List(
      nameField,
      money,
      sep1,
      menu,
      sep2,
      panel)
  }

  /** Displays stock panel. */
  private def displayStock(): Unit = {
    panel = stock
    setChildren()
  }

  /** Displays catalog panel. */
  private def displayModels(): Unit = {
    panel = models
    setChildren()
  }

  /** Update the stock subpanel. */
  private def updateStock(): Unit = {
    stock =
      new CompanyStock(company, world, detailTrain, detailEngine, detailCarriage)
    money.text = MoneyFormatter.format(company.money)
  }

  override def draw(): Unit = {
    money.text = MoneyFormatter.format(company.money)
    stock.draw()
  }
}
