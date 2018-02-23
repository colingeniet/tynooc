package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.event._
import javafx.scene.input.MouseEvent
import javafx.event.EventHandler

/** Clickabel label.
 *
 *  Text label with callback on click.
 *  @param _text the label text.
 *  @param callback the function called on click.
 */
class Link(_text: String)(callback: => Unit) extends Label {
  text = _text
  onMouseClicked = new EventHandler[MouseEvent] {
    override def handle(event: MouseEvent) {
      callback
    }
  }
}
