package gui.scenes.panes.facility

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._

import gui.scenes.panes._
import gui.scenes.panes.model._
import gui.scenes.elements._
import formatter._
import logic.facility._


class FactoryModelStats(model: FactoryModel)
extends FacilityModelStats(model) {
  override def filter(a: java.lang.reflect.Field): Boolean = {
    a.getName() == "consumes" ||
    a.getName() == "produces" ||
    a.getName() == "cycleTime" || super.filter(a)
  }

  children.add(new VBox {
    children = new Label("consumes:") ::
      model.consumes.toList.map{case (g,v) => new Label(s" ${g}: ${v}")}
  }.delegate)

  children.add(new VBox {
    children = new Label("produces:") ::
      model.produces.toList.map{case (g,v) => new Label(s" ${g}: ${v}")}
  }.delegate)

  children.add(new Label(
    s"cycle length: ${TimeFormatter.timeToDateString(model.cycleTime)}"
  ).delegate)
}
