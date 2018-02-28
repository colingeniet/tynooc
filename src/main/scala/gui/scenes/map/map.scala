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
import logic.game._
import logic.world._
import logic.town._
import logic.route._
import logic.travel._


/** Main map class.
 *
 *  @param world the world to display.
 *  @param displayTown callback function used to display town details.
 *  @param displayRoute callback function used to display route details.
 */
class Map(val world: World, displayTown: Town => Unit, displayRoute: Route => Unit)
extends ScrollPane with Drawable {
  /* Actual content, inside a ZoomPane.
     The ScrollPane is only a container. */
  private object MapContent extends ZoomPane with Drawable {
    private var routesMap: Pane = new Pane {
      // don't catch clicks on background
      pickOnBounds = false
    }
    private var townsMap: Pane = new Pane {
      // don't catch clicks on background
      pickOnBounds = false
    }
    private var dynamicMap: Pane = new Pane {
      // don't catch clicks on background
      pickOnBounds = false
    }
    private var textMap: Pane = new Pane {
      // don't catch clicks
      mouseTransparent = true
    }

    children = new StackPane {
      children = List(routesMap, townsMap, dynamicMap, textMap)
    }

    // display all towns and routes
    world.towns.foreach {
      town => {
        addTown(town)
        town.routes.foreach(addRoute(_))
      }
    }

    // options
    styleClass.add("map")
    minScale = 0.2
    maxScale = 4

    /** Display a town. */
    private def addTown(town: Town): Unit = {
      // town is displayed as a point
      var point: Circle = new Circle() {
        centerX = town.x
        centerY = town.y
        radius = 12
        fill = Black
        onMouseClicked = new EventHandler[MouseEvent] {
          override def handle(event: MouseEvent) {
            displayTown(town)
          }
        }
      }
      townsMap.children.add(point)

      // text field for town name
      // FIXME : display on top
      var text: Text = new Text(town.x + 9, town.y - 9, town.name) {
        mouseTransparent = true
        styleClass.add("town-name")
      }
      textMap.children.add(text)
    }

    /** Display a route. */
    private def addRoute(route: Route): Unit = {
      var line: Line = new Line() {
        startX = route.start.x
        startY = route.start.y
        endX = route.end.x
        endY = route.end.y
        stroke = Black
        strokeWidth = 5
        onMouseClicked = new EventHandler[MouseEvent] {
          override def handle(event: MouseEvent) {
            displayRoute(route)
          }
        }
      }
      routesMap.children.add(line)
    }

    /** Display a train. */
    private def drawTrain(travel: Travel): Circle = {
      // Coordonates of start and end towns
      val x1 = travel.currentRoute.start.x
      val y1 = travel.currentRoute.start.y
      val x2 = travel.currentRoute.end.x
      val y2 = travel.currentRoute.end.y
      val p = travel.currentRouteProportion
      // train coordonates
      val x = x1 * (1-p) + x2 * p
      val y = y1 * (1-p) + y2 * p
      new Circle() {
        centerX = x
        centerY = y
        radius = 8
        fill = Red
      }
    }

    override def draw(): Unit = {
      dynamicMap.children = world.travels.map(drawTrain(_))
    }
  }

  content = MapContent
  // disable scroll
  vmax = 0
  hmax = 0
  hbarPolicy = ScrollPane.ScrollBarPolicy.Never
  vbarPolicy = ScrollPane.ScrollBarPolicy.Never

  override def draw(): Unit = MapContent.draw()
}
