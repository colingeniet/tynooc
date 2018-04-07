package gui.scenes.panes.facility

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.paint._
import scalafx.event._

import gui.scenes.panes._
import gui.scenes.panes.model._
import gui.scenes.elements._
import gui.scenes.color._
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


class FactoryDetail(factory: Factory) extends VBox(3) {
  private val company: Label = new Label {
    styleClass.remove("label")

    text <== createStringBinding(
      () => factory.owner().name,
      factory.owner)

    textFill <== createObjectBinding[javafx.scene.paint.Paint](
      () => Colors(factory.owner()).delegate,
      factory.owner)
  }

  private val status: Label = new Label {
    text <== createStringBinding(
      () => {
        if (factory.working()) "working"
        else "inactive"
      },
      factory.working)
  }

  private val model = new FactoryModelStats(factory.model)

  children = List(company, status, model)
}
