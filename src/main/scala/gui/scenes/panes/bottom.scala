/* Bottom pane : town/route display */

package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry._

import gui.scenes.elements.Link
import logic.world._

/** Bottom panel display abstract class. */
abstract class Bottom extends HBox(5) {
  update()
  /** Update display. */
  def update(): Unit
}

/** Town display panel.
 *
 *  @param town the town to display.
 */
class Town(town: World.Town, displayRoute: World.Route => Unit)
extends Bottom {
  def update(): Unit = {
    children = List(
      new Label(town.name),
      new Separator{ orientation = Orientation.Vertical },
      new Label("Population : " + town.population),
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
  }
}

/** Town display panel.
 *
 *  @param route the route to display.
 *  @param displayTown callback used to display a town.
 */
class Route(route: World.Route, displayTown: World.Town => Unit)
extends Bottom {
  def update(): Unit = {
    children = List(
      new Link(route.start.name)(displayTown(route.start)),
      new Label("-"),
      new Link(route.end.name)(displayTown(route.end)),
      new Separator{ orientation = Orientation.Vertical },
      new Label("Distance : " + route.length)
    )
  }
}
