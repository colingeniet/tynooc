package gui.scenes.map

import collection.mutable.HashMap

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.shape._
import scalafx.scene.text._
import scalafx.scene.layout._
import scalafx.scene.image._
import scalafx.scene.effect._
import scalafx.event._
import scalafx.geometry._
import javafx.scene.input.MouseEvent
import javafx.event.EventHandler
import scalafx.scene.paint.Color._
import scalafx.beans.binding._
import scalafx.beans.binding.BindingIncludes._
import scalafx.collections._

import gui.scenes.elements._
import gui.scenes.color._
import gui._
import logic.game._
import logic.world._
import logic.town._
import logic.route._
import logic.travel._
import logic.company._
import logic.vehicle._

import scala.util.Try


/* Actual content, inside a ZoomPane.
   The ScrollPane is only a container. */
class MapContent(
  val world: World,
  val company: Company,
  displayTown: Town => Unit,
  displayRoute: Route => Unit,
  displayTravel: Travel => Unit)
extends StackPane with ZoomPane {

  val routesMiddle: HashMap[Route, Point2D] = new HashMap()
  val planesRoute: HashMap[Travel, Line] = new HashMap()

  class MapTravel(val travel: Travel, color: Double)
  extends ImageView(Resources.images(travel.vehicle)) {
    effect = new ColorAdjust(color, 0.0, 0.0, 0.0)
    onMouseClicked = new EventHandler[MouseEvent] {
      override def handle(event: MouseEvent) {
        displayTravel(travel)
      }
    }

    travel.vehicle match {
      case _: Plane => rotate <== travel.heading
      case _ => ()
    }


    private def deleteIfDone(): Unit = {
      if(travel.isDone()) {
        disable = true
        vehicleMap.children.remove(this)
        /*if(travel.company == company)
          Try(Resources.sound.get.play(1.0))*/
        travel.vehicle match {
          case p: Plane => routesMap.children.remove(planesRoute(travel))
          case _ => ()
        }
      }
    }

    x <== scale * travel.posX
    y <== scale * travel.posY

    private def relativePosition(p: Double, start: Double, middle: Double, end: Double): Double = {
      if(p < 0.5)
        start + 2 * p * (middle - start)
      else
        middle + (p - 0.5) * 2 * (end - middle)
    }

    private def changePosition(): Unit = {
      travel.currentRoute() match {
        case Some(r) => {
          val t = travel.currentRouteProportion.toDouble
          val a = r.start
          val c = r.end
          val b = travel.vehicle match {
            case p: Plane => new Point2D((a.x + c.x) / 2, (a.y + c.y) / 2)
            case p: Tank => new Point2D((a.x + c.x) / 2, (a.y + c.y) / 2)
            case _ => routesMiddle(r)
          }
          x <== { scale * ((1-t)*(1-t)*a.x + 2*t*(1-t)*b.x+t*t*c.x) - image().getWidth()/2 }
          y <== { scale * ((1-t)*(1-t)*a.y + 2*t*(1-t)*b.y+t*t*c.y) - image().getWidth()/2 }
        }
        case _ => ()
      }
    }


    travel.isDone.onChange(deleteIfDone())
    travel.currentRouteProportion.onChange(changePosition())
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

  minScale = 0.05
  maxScale = 10

  /** Display a town. */
  private def addTown(town: Town): Unit = {
    // town is displayed as a point
    val point: Circle = new Circle() {
      centerX <== scale * town.x
      centerY <== scale * town.y
      radius = 8
      fill = Black
      onMouseClicked = new EventHandler[MouseEvent] {
        override def handle(event: MouseEvent) {
          displayTown(town)
        }
      }
    }
    townsMap.children.add(point)

    // text field for town name
    val text: Text = new Text(town.name) {
      x <== (scale * town.x) + 6
      y <== (scale * town.y) - 6
      mouseTransparent = true
      styleClass.add("town-name")
    }
    textMap.children.add(text)
  }

  def angles(route: Route): Double = {
    route match {
      case r: Rail => Math.PI / 18
      case c: Canal => Math.PI / 15
      case r: River => Math.PI / 12
      case s: Seaway => Math.PI / 9
      case _         => 0
    }
  }

  def routeColor(route: Route) = {
    route match {
      case r: Road => Black
      case r: Rail => Gray
      case c: Canal => RoyalBlue
      case r: River => Aquamarine
      case s: Seaway => Blue
      case a: Airway => White
    }
  }

  def XRouteDirection(route: Route): Int = {
    route match {
      case c: Canal  => -1
      case s: Seaway => -1
      case r         =>  1
    }
  }

  def routeStrokeWidth(route: Route): Double = {
    route match {
      case r: Road => 4
      case s: Seaway => 4
      case r: Rail => 3
      case c: Canal => 3
      case r: River => 3
      case a: Airway => 3
    }
  }


  /** Display a route. */
  private def addRoute(route: Route): Unit = {
    val A = if(route.start.x < route.end.x) route.start else route.end
    val B = if(A == route.start) route.end else route.start
    val C = new Point2D((A.x + B.x) / 2, (A.y + B.y) / 2)
    val distX = (B.x - A.x)
    val distY = (B.y - A.y)
    val dist = math.hypot(distX, distY)
    val L = Math.tan(angles(route)) * (dist / 2)
    val CD = new Point2D(XRouteDirection(route) * L * (B.y - A.y) / dist,
                         - XRouteDirection(route) * L * (B.x - A.x) / dist)
    val AD = new Point2D(C.x - A.x + CD.x, C.y - A.y + CD.y)
    val middle =  new Point2D(AD.x + A.x, AD.y + A.y)
    routesMiddle(route) = middle

    val curve = new QuadCurve {
      controlX <== scale * middle.x
      controlY <== scale * middle.y
      startX <== scale * A.x
      startY <== scale * A.y
      endX <== scale * B.x
      endY <== scale * B.y
      stroke = routeColor(route)
      fill = null
      strokeWidth = routeStrokeWidth(route)
      onMouseClicked = new EventHandler[MouseEvent] {
        override def handle(event: MouseEvent) {
          displayRoute(route)
        }
      }
    }
    routesMap.children.add(curve)
  }

  /** Add a new travel. */
  private def addTravel(travel: Travel): Unit = {
    var hue = Colors(travel.company).hue / 360.0
    // scalafx expect shift between -1 and 1 (0 is no change)
    if (hue > 0.5) hue = 2 * hue - 2
    else hue = 2 * hue
    travel.vehicle match {
      case p: Plane => {
        val line = new Line() {
          startX <== scale * travel.currentRoute().get.start.x
          startY <== scale * travel.currentRoute().get.start.y
          endX <== scale * travel.currentRoute().get.end.x
          endY <== scale * travel.currentRoute().get.end.y
          stroke = Gray
          strokeWidth = 3
        }
        line.getStrokeDashArray().addAll(2d, 18d);
        routesMap.children.add(line)
        planesRoute(travel) = line
      }
      case _ => ()
    }
    vehicleMap.children.add(new MapTravel(travel, hue))
  }

  world.travels.foreach(addTravel(_))
  world.onAddTravel = addTravel

  // focus more on less on the map
  focus(world.minX, world.minY)
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
