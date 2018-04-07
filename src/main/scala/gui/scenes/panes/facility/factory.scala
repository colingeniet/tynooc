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


class ProductionDetail(prod: ProductionCycle)
extends VBox(3) {
  children = List (
    new VBox {
      children = new Label("consumes:") ::
        prod.consumes.toList.map{case (g,v) => new Label(s" ${g.name}: ${v}")}
    },
    new VBox {
      children = new Label("produces:") ::
        prod.produces.toList.map{case (g,v) => new Label(s" ${g.name}: ${v}")}
    },
    new Label(s"cycle length: ${TimeFormatter.timeToHourString(prod.cycleTime)}"))
}

class FactoryModelStats(model: FactoryModel)
extends FacilityModelStats(model) {
  override def filter(a: java.lang.reflect.Field): Boolean = {
    a.getName() == "productions" || super.filter(a)
  }

  children.add(new VBox {
    children = model.productions.map(new ProductionDetail(_))
  }.delegate)
}
