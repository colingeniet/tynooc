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
import gui.scenes.color._
import gui._
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
  object MapTravel {
    def icon(travel: Travel): String = {
      travel.vehicle match {
        case _: Engine => "/icons/train.png"
        case _: Plane => "/icons/plane.png"
        case _: Truck => "/icons/truck.png"
        case _: Ship => "/icons/ship.png"
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

    x <== travel.posX.toDouble
    y <== travel.posY.toDouble

    travel.vehicle match {
      case _: Plane => rotate <== travel.heading
      case _ => ()
    }

    private def deleteIfDone(): Unit = {
      if(travel.isDone()) {
        disable = true
        vehicleMap.children.remove(this)
        if(travel.company == company)
          Resources.Sound.get.play()
      }
    }

    private def changePosition(): Unit = {
      travel.currentRoute() match {
        case Some(r) => {
          val p = travel.currentRouteProportion.toDouble
          val A = if(r.start.x < r.end.x) r.start else r.end
          val B = if(A == r.start) r.end else r.start
          val mil = milRoute(A, B, angles(r), XRouteDirection(r))
          x <== ({
            val xMil = mil._1
            if(p < 0.5)
              r.start.x +  2 * p * (xMil - r.start.x)
            else
              xMil + (p - 0.5) * 2 * (r.end.x - xMil)
            }) - image().getWidth()/2
          y <== ({
            val yMil = mil._2
            if(p < 0.5)
              r.start.y +  2 * p * (yMil - r.start.y)
            else
              yMil + (p - 0.5) * 2 * (r.end.y - yMil)
            }) - image().getWidth()/2
          }
        case _ => ()
      }
    }


    travel.isDone.onChange(deleteIfDone())
    travel.currentRouteProportion.onChange(changePosition())
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
      case c: Canal => Blue
      case r: River => AliceBlue
      case s: Seaway => Aquamarine
      case a: Airway => White
    }
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

  def XRouteDirection(route: Route): Int = {
    route match {
      case c: Canal  => -1
      case s: Seaway => -1
      case r         =>  1
    }
  }

  def routeStrokeWidth(route: Route): Double = {
    route match {
      case r: Road => 5
      case r: Rail => 4
      case c: Canal => 4
      case r: River => 4
      case s: Seaway => 5
      case a: Airway => 5
    }
  }

  def milRoute(A: Town, B: Town, alpha: Double, XDirection: Int): (Double, Double) = {
    val Ax = A.x
    val Ay = A.y
    val Bx = B.x
    val By = B.y

    val Cx = (A.x + B.x) / 2
    val Cy = (A.y + B.y) / 2

    val distX = (B.x - A.x)
    val distY = (B.y - A.y)
    val dist = math.hypot(distX, distY)
    val L = Math.tan(alpha) * (dist / 2)
    val CDx = XDirection * L * (B.y - A.y) / dist
    val CDy = - XDirection * L * (B.x - A.x) / dist

    val ADx = Cx - Ax + CDx
    val ADy = Cy - Ay + CDy

    val Dx = ADx + Ax
    val Dy = ADy + Ay
    (Dx, Dy)
  }
  /** Display a route. */
  private def addRoute(route: Route): Unit = {
    val A = if(route.start.x < route.end.x) route.start else route.end
    val B = if(A == route.start) route.end else route.start
    val Ax = A.x
    val Ay = A.y
    val Bx = B.x
    val By = B.y

    val mil = milRoute(A, B, angles(route), XRouteDirection(route))
    val Dx = mil._1
    val Dy = mil._2

    val curve = new QuadCurve {
      controlX = Dx
      controlY = Dy
      startX = Ax
      startY = Ay
      endX = Bx
      endY = By
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

    vehicleMap.children.add(new MapTravel(travel, hue))
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
