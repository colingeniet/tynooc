/* Bottom pane : town/route display */

package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry._

import gui.draw._
import gui.scenes.elements.Link
import logic.world._


/** Town display panel.
 *
 *  @param town the town to display.
 *  @param displayRoute callback used to display a route.
 */
class TownInfo(town: World.Town, displayRoute: World.Route => Unit)
extends DrawableHBox {
  // needs to be updated at redraw
  private var popLbl = new Label("Population : " + town.population)

  children = List(
    new Label(town.name),
    new Separator{ orientation = Orientation.Vertical },
    popLbl,
    new Separator{ orientation = Orientation.Vertical },
    new Label("Routes to : ")
  )

  // add all clickable routes
  town.routes.foreach { route =>
    var label: Label = new Link(route.end.name + "(" + route.length + "), ")(
      displayRoute(route)
    )
    children.add(label)
  }

  spacing = 3

  // Only update population
  override def draw(): Unit = {
    popLbl.text = "Population : " + town.population
  }
}

/** Town display panel.
 *
 *  @param route the route to display.
 *  @param displayTown callback used to display a town.
 */
class RouteInfo(route: World.Route, displayTown: World.Town => Unit)
extends DrawableHBox {
  children = List(
    new Link(route.start.name)(displayTown(route.start)),
    new Label("-"),
    new Link(route.end.name)(displayTown(route.end)),
    new Separator{ orientation = Orientation.Vertical },
    new Label("Distance : " + route.length)
  )

  spacing = 3
}
