package gui.scenes.elements

import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.text._
import scalafx.geometry._

class MessagesBox(size: Int) extends Pane {
  val field = new Text {
    text = "\n"
  }

  def print(message: String): Unit = {
    field.text = field.text() + message + "\n"
    nMessages += 1
    if(nMessages > size) {
      val str = new StringBuilder(field.text())
      field.text = str.drop(str.indexOf("\n") + 1).toString
    }
  }

  private var nMessages: Int = 0

  children = field
  mouseTransparent = true
}
