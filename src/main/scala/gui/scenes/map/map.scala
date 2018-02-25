package gui.scenes.map

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.shape._
import scalafx.scene.text._
import scalafx.scene.layout._
import scalafx.event._
import javafx.scene.input.MouseEvent
import javafx.event.EventHandler
import scalafx.scene.paint.Color._

import gui.draw._
import gui.scenes.elements._
import logic.world.World


/** Main map class.
 *
 *  @param world the world to display.
 *  @param displayTown callback function used to display town details.
 *  @param displayRoute callback function used to display route details.
 */
class Map(
  world: World,
  displayTown: World.Town => Unit,
  displayRoute: World.Route => Unit)
extends ScrollPane with Drawable {
  /* Actual content, inside a ZoomPane.
     The ScrollPane is only a container. */
  private object Map extends ZoomPane with Drawable {
    // display all towns and routes
    world.towns.foreach {
      town => {
        addTown(town)
        town.routes.foreach{ route => addRoute(route) }
      }
    }

    // options
    styleClass.add("map")
    minScale = 0.2
    maxScale = 4

    /** Display a town. */
    private def addTown(town: World.Town): Unit = {
      // town is displayed as a point
      var point: Circle = new Circle()
      point.centerX = town.x
      point.centerY = town.y
      point.radius = 12
      point.fill = Black
      point.onMouseClicked = new EventHandler[MouseEvent] {
        override def handle(event: MouseEvent) {
          displayTown(town)
        }
      }
      children.add(point)

      // text field for town name
      // FIXME : display on top
      var text: Text = new Text(town.x + 9, town.y - 9, town.name) {
        mouseTransparent = true
        styleClass.add("town-name")
      }
      children.add(text)
    }

    /** Display a route. */
    private def addRoute(route: World.Route): Unit = {
      var line: Line = new Line()
      line.startX = route.start.x
      line.startY = route.start.y
      line.endX = route.end.x
      line.endY = route.end.y
      line.stroke = Black
      line.strokeWidth = 5
      line.onMouseClicked = new EventHandler[MouseEvent] {
        override def handle(event: MouseEvent) {
          displayRoute(route)
        }
      }
      children.add(line)
    }

    private def drawTrain(x1: Double, y1: Double, x2: Double, y2: Double, p: Double): Unit = {
      val x = x1 * (1-p) + x2 * p
      val y = y1 * (1-p) + y2 * p
      var point: Circle = new Circle()
      point.centerX = x
      point.centerY = y
      point.radius = 8
      point.fill = Red
      children.add(point)
    }
  }

  content = Map
  // disable scroll
  vmax = 0
  hmax = 0
  hbarPolicy = ScrollPane.ScrollBarPolicy.Never
  vbarPolicy = ScrollPane.ScrollBarPolicy.Never

  override def draw(): Unit = Map.draw()
}
