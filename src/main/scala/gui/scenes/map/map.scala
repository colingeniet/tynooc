package gui.scenes.map

import collection.mutable.HashMap

import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.shape._
import scalafx.scene.text._
import scalafx.scene.layout._
import scalafx.scene.image._
import scalafx.scene.effect._
import scalafx.event._
import javafx.scene.input.MouseEvent
import javafx.event.EventHandler
import scalafx.scene.paint.Color._

import scalafx.beans.binding._
import scalafx.beans.binding.BindingIncludes._
import scalafx.collections._

import gui.scenes.elements._
import logic.game._
import logic.world._
import logic.town._
import logic.route._
import logic.travel._
import logic.company._


/** Main map class.
 *
 *  @param world the world to display.
 *  @param displayTown callback function used to display town details.
 *  @param displayRoute callback function used to display route details.
 */
class Map(
  val world: World,
  val company: Company,
  displayTown: Town => Unit,
  displayRoute: Route => Unit,
  displayTravel: Travel => Unit)
extends ScrollPane {
  /** Player colors generator */
  object Colors {
    private var level: Int = 0
    private var pos: Int = 0

    /** Get next color.
     *
     * @return the hue to use with scalafx. */
    def nextColor(): Double = {
      // compute next hue between 0 and 1
      var hue: Double = 0.0
      if (level == 0) {
        // special initial comportment
        hue = pos.toDouble / 3
        pos += 1
        if (pos >= 3) {
          level = 3
          pos = 0
        }
      } else {
        hue = (pos + 0.5) / level
        pos += 1
        if (pos >= level) {
          level *= 2
          pos = 0
        }
      }

      // scalafx expect it between -1 and 1 (0 is no change)
      if (hue > 0.5) 2 * hue - 2
      else 2 * hue
    }
  }

  private var colors: HashMap[Company, Double] = new HashMap()
  colors(company) = Colors.nextColor()
  world.companies.filter(_ != company).map {
    colors(_) = Colors.nextColor()
  }

  /* Actual content, inside a ZoomPane.
     The ScrollPane is only a container. */
  private object MapContent extends ZoomPane {
    /** The dots representing a train on route on the map.
     *
     *  The `Circle` object is associated with the corresponding `Travel`
     *  to allow automatic updating of its position.
     */
    private class MapTravel(val travel: Travel) extends ImageView("/icons/train.png") {
      effect = new ColorAdjust(colors(travel.company), 0.0, 0.0, 0.0)
      onMouseClicked = new EventHandler[MouseEvent] {
        override def handle(event: MouseEvent) {
          displayTravel(travel)
        }
      }

      x <== travel.posX - 15
      y <== travel.posY - 15

      private def deleteIfDone(): Unit = {
        if(travel.isDone()) {
          disable = true
          dynamicMap.children.remove(this)
        }
      }

      travel.isDone.onChange(deleteIfDone())
    }

    // the list of current travels
    private var travels: ObservableBuffer[MapTravel] = ObservableBuffer()

    /* The map is organized as several superposed layers. */
    // map layers, from bottom to top :
    private val routesMap: Pane = new Pane {
      // don't catch clicks on background
      pickOnBounds = false
    }
    private val townsMap: Pane = new Pane {
      // don't catch clicks on background
      pickOnBounds = false
    }
    /* This is the only dynamic layer, that is the only one that
     * requires updating. It contains the trains.
     */
    private val dynamicMap: Pane = new Pane {
      // don't catch clicks on background
      pickOnBounds = false
    }
    private val textMap: Pane = new Pane {
      // don't catch clicks
      mouseTransparent = true
    }

    // layers are added to a StackPane to allow superposition
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

    // style options
    styleClass.add("map")
    minScale = 0.2
    maxScale = 4

    /** Display a town. */
    private def addTown(town: Town): Unit = {
      // town is displayed as a point
      val point: Circle = new Circle() {
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
      val text: Text = new Text(town.x + 9, town.y - 9, town.name) {
        mouseTransparent = true
        styleClass.add("town-name")
      }
      textMap.children.add(text)
    }

    /** Display a route. */
    private def addRoute(route: Route): Unit = {
      val line: Line = new Line() {
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

    /** Add a new travel. */
    private def addTravel(travel: Travel): Unit = {
      dynamicMap.children.add(new MapTravel(travel))
    }

    world.onAddTravel = addTravel
  }

  content = MapContent
  // disable scroll
  vmax = 0
  hmax = 0
  hbarPolicy = ScrollPane.ScrollBarPolicy.Never
  vbarPolicy = ScrollPane.ScrollBarPolicy.Never
}
