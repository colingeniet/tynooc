package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._

import gui.draw._

class Menu(quitBtn: Button) extends DrawableHBox {
  children = List(quitBtn)
}
