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


class PlayerInfo(
  player: Player,
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
  menu.addMenu("assemble", assemblePane())

  private var stock: PlayerStock =
    new PlayerStock(player, detailTrain, detailEngine, detailCarriage)

  private var models: ModelsList = new ModelsList(player, updateStock)

  spacing = 5
  draw()
  setChildren()


  private def setChildren(): Unit = {
    children = List(
      money,
      sep1,
      menu,
      sep2,
      panel)
  }

  private def displayStock(): Unit = {
    panel = stock
    setChildren()
  }

  private def displayModels(): Unit = {
    panel = models
    setChildren()
  }

  private def assemblePane(): Unit = {
    panel = new Label("assemble")
    setChildren()
  }

  private def updateStock(): Unit = {
    stock = new PlayerStock(player, detailTrain, detailEngine, detailCarriage)
    money.text = player.money + "$"
  }

  override def draw(): Unit = {
    money.text = player.money + "$"
  }
}
