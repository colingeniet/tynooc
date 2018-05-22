package gui.scenes.panes.model

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._

import gui.scenes.elements._
import logic.model._
import logic.company._
import formatter._


/** Generic attributes display.
 *
 *  @param m the object to display.
 */
class Stats(m: Object) extends VBox(3) {
  /** This method may be overloaded to avoid displaying some attributes. */
  def filter(a: java.lang.reflect.Field): Boolean = false

  /** This method may be overloaded to change the display of some attributes. */
  def display(a: java.lang.reflect.Field): Node = {
    new Label(s"${StringFormatter.casePrettyPrint(a.getName())}: ${a.get(m).toString}")
  }

  // display all attributes using reflexivity
  children = m.getClass().getDeclaredFields.toList.map {
      // obscure java stuff to access attributes
      a => { a.setAccessible(true); a }
    }.filterNot(this.filter(_)).map(this.display(_))
}


/** Model attributes display. */
class ModelStats(m: Model) extends Stats(m) {
  override def display(a: java.lang.reflect.Field): Node = {
    if(a.getName() == "name") new Label(a.get(m).toString)
    else super.display(a)
  }
}

class BuyableModelStats(m: BuyableModel) extends ModelStats(m) {
  override def display(a: java.lang.reflect.Field): Node = {
    if(a.getName() == "upgrades") {
      val str: StringBuilder = new StringBuilder(a.get(m).toString)
      // attribute 'upgrades' is a list of strings, but this is not known here,
      // this is a disgusting hack to get proper formatting of it
      // List is formatted as "List(elem, ...)"
      // remove the inilial "List(" and final ")"
      new Label("upgrades: " + str.drop(str.indexOf("(")+1).dropRight(1).toString)
    } else super.display(a)
  }
}

/* Do not display price and upgrades */
class BuyableModelShortStats(m: BuyableModel) extends ModelStats(m) {
  override def filter(a: java.lang.reflect.Field): Boolean = {
    a.getName() == "price" || a.getName() == "upgrades" || super.filter(a)
  }
}


/** Generic upgrade menu for buyable/upgradable objects. */
class UpgradeMenu[Model <: BuyableModel](
  thing: Upgradable[Model],
  company: Company)
extends VBox(3) { menu =>
  val upgradeButton: Button = new Button("Upgrade") {
    onAction = (event: ActionEvent) => {
      // when pressing the button, display the list of upgrades
      val selectionList: SelectionList[String] =
        new SelectionList[String](
          thing.model.upgrades,
          name => s"${name}(${MoneyFormatter.format(PriceSimulation.upgradePrice(thing, name))})",
          name => {
            // upgrade engine upon selection
            company.upgrade(thing, name)
            // reset content
            menu.setChildren()
        })

      // display new selection list upon button pressed
      menu.setChildren()
      children.add(selectionList)
    }

    disable <== thing.owner =!= company
  }

  /** This function may be oveloaded to change the pane content. */
  def setChildren(): Unit = {
    children = List(upgradeButton)
  }
}
