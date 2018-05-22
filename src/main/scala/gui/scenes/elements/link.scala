package gui.scenes.elements

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.event._

/** Clickabel label.
 *
 *  Text label with callback on click.
 *  @param text the label text.
 *  @param callback the function called on click.
 */
class Link(text: String, callback: () => Unit) extends Button(text) {
  onAction = (event: ActionEvent) => callback()
  styleClass.remove("button")
  styleClass.add("link")
}
