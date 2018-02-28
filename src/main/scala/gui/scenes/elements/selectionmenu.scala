package gui.scenes.elements

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._

class SelectionMenu extends VBox(3) {
  private var group: ToggleGroup = new ToggleGroup()

  def addMenu(text: String, action: => Unit): Unit = {
    var button = new RadioButton(text) {
      onAction = (event: ActionEvent) => action
      styleClass.remove("radio-button")
      styleClass.add("link")
    }
    group.toggles.add(button)
    children.add(button)
  }
}

class SelectionList[A](
  list: List[A],
  text: A => String,
  action: A => Unit)
extends ScrollPane {
  private var menu: SelectionMenu = new SelectionMenu()
  list.foreach(a => menu.addMenu(text(a), action(a)))
  content = menu
}
