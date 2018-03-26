package gui.scenes.panes.model

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._

import logic.model._


/** Generic attributes display.
 *
 *  @param m the object to display.
 */
class Stats[M](m: M) extends VBox(3) {
  def filter(a: java.lang.reflect.Field): Boolean = false
  def display(a: java.lang.reflect.Field): Node = {
    new Label(a.getName() + ": " + a.get(m).toString)
  }

  children = m.getClass().getDeclaredFields.toList.map {
      // obscure java stuff to access attributes
      a => { a.setAccessible(true); a }
    }.filterNot(this.filter(_)).map(this.display(_))
}

class ModelStats[M <: Model](m: M) extends Stats[M](m) {
  def nameSuffix: Option[String] = None

  override def display(a: java.lang.reflect.Field): Node = {
    if(a.getName() == "name") {
      val nameStr = this.nameSuffix match {
        case Some(suf) => a.get(m).toString + " " + suf
        case None => a.get(m).toString
      }
      new Label(nameStr)
    } else super.display(a)
  }
}

class BuyableModelStats[M <: BuyableModel](m: M) extends ModelStats[M](m) {
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
class BuyableModelShortStats[M <: BuyableModel](m: M) extends ModelStats[M](m) {
  override def filter(a: java.lang.reflect.Field): Boolean = {
    a.getName() == "price" || a.getName() == "upgrades" || super.filter(a)
  }
}
