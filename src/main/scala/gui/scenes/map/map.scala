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
import logic.vehicle._




/* Actual content, inside a ZoomPane.
   The ScrollPane is only a container. */
class MapContent(
  val world: World,
  val company: Company,
  displayTown: Town => Unit,
  displayRoute: Route => Unit,
  displayTravel: Travel => Unit)
extends StackPane with ZoomPane {
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


  object MapTravel {
    def icon(travel: Travel): String = {
      travel.vehicle match {
        case _: Engine => "/icons/train.png"
        case _: Plane => "/icons/plane.png"
        case _ => "/icons/train.png"
      }
    }
  }

  class MapTravel(val travel: Travel, color: Double)
  extends ImageView(MapTravel.icon(travel)) {
    effect = new ColorAdjust(color, 0.0, 0.0, 0.0)
    onMouseClicked = new EventHandler[MouseEvent] {
      override def handle(event: MouseEvent) {
        displayTravel(travel)
      }
    }

    x <== travel.posX - image().getWidth()/2
    y <== travel.posY - image().getHeight()/2

    travel.vehicle match {
      case _: Plane => rotate <== travel.heading
      case _ => ()
    }

    private def deleteIfDone(): Unit = {
      if(travel.isDone()) {
        disable = true
        vehicleMap.children.remove(this)
      }
    }

    travel.isDone.onChange(deleteIfDone())
  }


  // the list of current travels
  private var travels: ObservableBuffer[MapTravel] = ObservableBuffer()

  private var colors: HashMap[Company, Double] = new HashMap()
  colors(company) = Colors.nextColor()
  world.companies.filter(_ != company).map {
    colors(_) = Colors.nextColor()
  }

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
  private val vehicleMap: Pane = new Pane {
    // don't catch clicks on background
    pickOnBounds = false
  }
  private val textMap: Pane = new Pane {
    // don't catch clicks
    mouseTransparent = true
  }

  children = List(routesMap, townsMap, vehicleMap, textMap)

  // display all towns and routes
  world.towns.foreach {
    town => {
      addTown(town)
      town.routes.foreach(addRoute(_))
    }
  }

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
    vehicleMap.children.add(new MapTravel(travel, colors(travel.company)))
  }

  world.onAddTravel = addTravel
}


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
extends ZoomPaneContainer(new MapContent(world, company, displayTown, displayRoute, displayTravel)) {
  styleClass.add("map")
}
