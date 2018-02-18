package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.event._
import scalafx.scene.layout._
import scalafx.geometry._
import scalafx.scene.paint._

class Menu(quitBtn: Button) extends HBox {
  children = List(quitBtn)
}
