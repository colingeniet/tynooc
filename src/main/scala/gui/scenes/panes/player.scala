package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._

import java.util.{Currency, Locale}

import gui.draw._
import gui.scenes.elements._
import logic.player._
import logic.train._
import logic.world._

/** Player information panel.
 *
 *  Allows to view player's rolling stock, buy new stock,
 *  assemble trains ...
 *  @param player the player.
 *  @param detailTrain callback used to display info on a train in another panel.
 *  @param detailEngine callback used to display info on an engine in another panel.
 *  @param detailCarriage callback used to display info on a carriage in another panel.
 */
class PlayerInfo(
  player: Player,
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
  private val formatter = java.text.NumberFormat.getCurrencyInstance
  private val fr = Currency.getInstance(new Locale("fr", "FR"))
  formatter.setCurrency(fr)
  
  menu.addMenu("rolling stock", displayStock())
  menu.addMenu("catalog", displayModels())

  // stock subpanel
  private var stock: PlayerStock =
    new PlayerStock(player, world, detailTrain, detailEngine, detailCarriage)

  // model catalog subpanel
  private val models: ModelsList = new ModelsList(player, updateStock)

  spacing = 5
  draw()
  setChildren()

  /* Updates children list from attributes. */
  private def setChildren(): Unit = {
    children = List(
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
      new PlayerStock(player, world, detailTrain, detailEngine, detailCarriage)
    money.text = formatter.format(player.money)
  }

  override def draw(): Unit = {
    money.text = formatter.format(player.money)
    stock.draw()
  }
}
