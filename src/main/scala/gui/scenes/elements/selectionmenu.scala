package gui.scenes.elements

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._
import scalafx.beans.property._
import scalafx.collections._

import utils._

/** A list of submenus, only one of which can be selected at a time.
 *
 *  The submenus are displayed in a vertical list. */
class SelectionMenu extends VBox(3) {
  private var group: ToggleGroup = new ToggleGroup()

  /** Add a new submenu.
   *
   *  @param button the button to add as a submenu.
   */
  def addMenu(button: ToggleButton): Unit = {
    button.styleClass.remove("toggle-button")
    button.styleClass.add("link")
    group.toggles.add(button)
    children.add(button)
  }

  /** Add a new submenu.
   *
   *  @param text the title of the submenu.
   *  @param action a callback called when the submenu is selected.
   */
  def addMenu(text: String, action: => Unit): Unit = {
    val button = new ToggleButton(text) {
      onAction = (event: ActionEvent) => {
        action
        selected = true
      }
    }
    addMenu(button)
  }

  /** Add a new submenu.
   *
   *  @param textProp the title of the submenu, as a string property.
   *  @param action a callback called when the submenu is selected.
   */
  def addMenu(textProp: StringProperty, action: => Unit): Unit = {
    val button = new ToggleButton {
      text <== textProp
      onAction = (event: ActionEvent) => {
        action
        selected = true
      }
    }
    addMenu(button)
  }

  /** Deselect all entries. */
  def deselect(): Unit = {
    group.toggles.foreach(_.selected = false)
  }
}



class SelectionListDynamic[A](
  list: ObservableBuffer[A],
  label: A => StringProperty,
  action: A => Unit)
extends ScrollPane {
  private val group: ToggleGroup = new ToggleGroup()

  private def createButton(a: A): ToggleButton = {
    val button = new ToggleButton {
      text <== label(a)
      onAction = (event: ActionEvent) => {
        action(a)
        selected = true
      }
      styleClass.remove("toggle-button")
      styleClass.add("link")
    }
    button
  }

  private val buttonList: ObservableBuffer[ToggleButton] = ObservableBuffer[ToggleButton]()
  MapBind(list, buttonList, createButton(_))

  private val contentBox: VBox = new VBox(3)
  content = contentBox

  MapBind(buttonList, contentBox.children, (b: ToggleButton) => b.delegate)
  MapBind(buttonList, group.toggles, (b: ToggleButton) => b.delegate)

  def this(list: List[A], label: A => StringProperty, action: A => Unit) =
    this(ObservableBuffer(list), label, action)

  def nth(n: Int): ToggleButton = buttonList(n)
  def deselect(): Unit = buttonList.foreach(_.selected = false)
}


class SelectionList[A](
  list: ObservableBuffer[A],
  label: A => String,
  action: A => Unit)
extends SelectionListDynamic(list, (a: A) => StringProperty(label(a)), action) {
  def this(list: List[A], label: A => String, action: A => Unit) =
    this(ObservableBuffer(list), label, action)
}
