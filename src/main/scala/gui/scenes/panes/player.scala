package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._

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
  private var money: Label = new Label()
  private var sep1: Separator = new Separator()
  private var menu: SelectionMenu = new SelectionMenu()
  private var sep2: Separator = new Separator()
  private var panel: Node = new Pane()

  menu.addMenu("stock", displayStock())
  menu.addMenu("models", displayModels())

  // stock subpanel
  private var stock: PlayerStock =
    new PlayerStock(player, world, detailTrain, detailEngine, detailCarriage)

  // model catalog subpanel
  private var models: ModelsList = new ModelsList(player, updateStock)

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
    money.text = player.money + "$"
  }

  override def draw(): Unit = {
    money.text = player.money + "$"
  }
}
