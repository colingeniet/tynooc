/* Bottom pane : town/route display */

package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry._

import gui.scenes.elements._
import gui.scenes.panes.model._
import gui.scenes.panes.facility._
import logic.town._
import logic.route._
import logic.facility._
import logic.company._
import logic.good._


/** Town display panel.
 *
 *  @param town the town to display.
 *  @param displayRoute callback used to display a route.
 */
class TownInfo(
  town: Town,
  company: Company,
  displayRoute: Route => Unit,
  displayFacility: Facility => Unit,
  displayStock: Town => Unit)
extends VBox(3) {
  private val popLbl = new Label {
    text <== createStringBinding(
      () => s"Population: ${town.population.toInt}",
      town.population)
  }
  private val pasLbl = new Label {
    text <== createStringBinding(
      () => s"Passengers: ${town.passengersNumber.toInt}",
      town.passengersNumber)
  }

  private var goods = new Link("goods stock", () => displayStock(town))

  private val routes = new VBox {
    children = new Label("Routes to:") :: town.routes.map{ route => new Link(
        f"${route.end.name} - ${route.length}%.0f (${route.name})",
        () => displayRoute(route))
    }
  }

  private val facilities = new SelectionList[Facility](
    town.facilities,
    _.model.name,
    detailFacility(_))


  private def setChildren(): Unit = {
    children = List(
      new Label(town.name),
      popLbl,
      pasLbl,
      goods,
      routes,
      facilities)
  }

  private def detailFacility(facility: Facility): Unit = {
    setChildren()
    children.add(FacilityMenu(facility, company))
    displayFacility(facility)
  }

  setChildren()
}

/** Town display panel.
 *
 *  @param route the route to display.
 *  @param displayTown callback used to display a town.
 */
class RouteInfo(route: Route, displayTown: Town => Unit)
extends VBox(3) {
  children = List(
    new Label(route.name),
    new Link(route.start.name, () => displayTown(route.start)),
    new Link(route.end.name, () => displayTown(route.end)),
    new Label(f"length: ${route.length}%.0f"),
    new Stats(route) {
      override def filter(a: java.lang.reflect.Field): Boolean = {
        a.getName() == "start" || a.getName() == "end" ||
          a.getName() == "length" || super.filter(a)
      }
    })
}


class TownStock(town: Town) extends ScrollPane {
  content = new VBox {
    children = Good.all.map(g => {
      new Label {
        text <== createStringBinding(
          () => f"${g.name}: ${town.goods(g)()}%.1f",
          town.goods(g))
      }
    })
  }
}
