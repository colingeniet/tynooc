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


/** Town display panel.
 *
 *  @param town the town to display.
 *  @param displayRoute callback used to display a route.
 */
class TownInfo(
  town: Town,
  company: Company,
  displayRoute: Route => Unit,
  displayFacility: Facility => Unit)
extends VBox(3) {
  private val popLbl = new Label {
    text <== createStringBinding(
      () => s"Population : ${town.population.toInt}",
      town.population)
  }
  private val pasLbl = new Label {
    text <== createStringBinding(
      () => s"Passengers : ${town.passengersNumber.toInt}",
      town.passengersNumber)
  }

  private val routes = new VBox {
    children = new Label("Routes to:") :: town.routes.map{ route => new Link(
        f"${route.end.name} - ${route.length}%.0f (${route.name})")(
        displayRoute(route))
    }
  }

  private val facilities = new SelectionList[Facility](
    town.facilities,
    _.model.name,
    facility => {
      setChildren()
      children.add(FacilityMenu(facility, company))
      displayFacility(facility)
    })


  private def setChildren(): Unit = {
    children = List(
      new Label(town.name),
      popLbl,
      pasLbl,
      routes,
      facilities)
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
    new Link(route.start.name)(displayTown(route.start)),
    new Link(route.end.name)(displayTown(route.end)),
    new Label(f"length: ${route.length}%.0f"),
    new Stats(route) {
      override def filter(a: java.lang.reflect.Field): Boolean = {
        a.getName() == "start" || a.getName() == "end" ||
          a.getName() == "length" || super.filter(a)
      }
    })
}
