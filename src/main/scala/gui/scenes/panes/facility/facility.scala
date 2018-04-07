package gui.scenes.panes.facility

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._
import scalafx.scene.paint._

import gui.scenes.panes._
import gui.scenes.panes.model._
import gui.scenes.elements._
import gui.scenes.color._
import formatter._
import logic.facility._
import logic.company._


class FacilityModelStats(model: FacilityModel)
extends BuyableModelStats(model)

object FacilityModelStats {
  def apply(facility: FacilityModel): FacilityModelStats = {
    facility match {
      case f: FactoryModel => new FactoryModelStats(f)
      case f => new FacilityModelStats(f)
    }
  }
}


class FacilityDetail(facility: Facility) extends VBox(3) {
  private val company: Label = new Label {
    styleClass.remove("label")

    text <== createStringBinding(
      () => facility.owner().name,
      facility.owner)

    textFill <== createObjectBinding[javafx.scene.paint.Paint](
      () => Colors(facility.owner()).delegate,
      facility.owner)
  }

  children = List(
    company,
    FacilityModelStats(facility.model))
}

object FacilityDetail {
  def apply(facility: Facility): VBox = {
    facility match {
      case f: Factory => new FactoryDetail(f)
      case f => new FacilityDetail(f)
    }
  }
}


class FacilityMenu(facility: Facility, company: Company)
extends UpgradeMenu[FacilityModel](facility, company) { menu =>
  private val buyButton: Button = new Button(s"buy(${facility.model.price})") {
    onAction = (event: ActionEvent) => {
      company.buy(facility)
      menu.setChildren()
    }

    disable <== company.money < facility.model.price
  }

  override def setChildren(): Unit = {
    super.setChildren()
    if (facility.owner() != company) children.add(buyButton)
  }
}

object FacilityMenu {
  def apply(facility: Facility, company: Company): FacilityMenu = {
    val menu = new FacilityMenu(facility, company)
    menu.setChildren()
    menu
  }
}
