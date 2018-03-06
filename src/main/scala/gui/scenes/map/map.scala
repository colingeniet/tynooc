package gui.scenes.map

import collection.mutable.HashMap

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
extends ScrollPane with Drawable {
  
  private object ColorManager {
    val colors = Array(
    AliceBlue,
    AntiqueWhite,
    Aqua,
    Aquamarine,
    Azure,
    Beige,
    Bisque,
    Black,
    BlanchedAlmond,
    Blue,
    BlueViolet,
    Brown,
    Burlywood,
    CadetBlue,
    Chartreuse,
    Chocolate,
    Coral,
    CornflowerBlue,
    Cornsilk,
    Crimson,
    Cyan,
    DarkBlue,
    DarkCyan,
    DarkGoldenrod,
    DarkGray,
    DarkGreen,
    DarkGrey,
    DarkKhaki,
    DarkMagenta,
    DarkOliveGreen,
    DarkOrange,
    DarkOrchid,
    DarkRed,
    DarkSalmon,
    DarkSeaGreen,
    DarkSlateBlue,
    DarkSlateGray,
    DarkSlateGrey,
    DarkTurquoise,
    DarkViolet,
    DeepPink,
    DeepSkyBlue,
    DimGray,
    DimGrey,
    DodgerBlue,
    FireBrick,
    FloralWhite,
    ForestGreen,
    Fuchsia,
    Gainsboro,
    GhostWhite,
    Gold,
    Goldenrod,
    Gray,
    Green,
    GreenYellow,
    Grey,
    Honeydew,
    HotPink,
    IndianRed,
    Indigo,
    Ivory,
    Khaki,
    Lavender,
    LavenderBlush,
    LawnGreen,
    LemonChiffon,
    LightBlue,
    LightCoral,
    LightCyan,
    LightGoldrenrodYellow,
    LightGray,
    LightGreen,
    LightGrey,
    LightPink,
    LightSalmon,
    LightSeaGreen,
    LightSkyBlue,
    LightSlateGray,
    LightSlateGrey,
    LightSteelBlue,
    LightYellow,
    Lime,
    LimeGreen,
    Linen,
    Magenta,
    Maroon,
    MediumAquamarine,
    MediumBlue,
    MediumOrchid,
    MediumPurple,
    MediumSeaGreen,
    MediumSlateBlue,
    MediumSpringGreen,
    MediumTurquoise,
    MediumVioletRed,
    MidnightBlue,
    MintCream,
    MistyRose,
    Moccasin,
    NavajoWhite,
    Navy,
    OldLace,
    Olive,
    OliveDrab,
    Orange,
    OrangeRed,
    Orchid,
    PaleGoldrenrod,
    PaleGreen,
    PaleTurquoise,
    PaleVioletRed,
    PapayaWhip,
    PeachPuff,
    Peru,
    Pink,
    Plum,
    PowderBlue,
    Purple,
    Red,
    RosyBrown,
    RoyalBlue,
    SaddleBrown,
    Salmon,
    SandyBrown,
    SeaGreen,
    SeaShell,
    Sienna,
    Silver,
    SkyBlue,
    SlateBlue,
    SlateGray,
    SlateGrey,
    Snow,
    SpringGreen,
    SteelBlue,
    Tan,
    Teal,
    Thistle,
    Tomato,
    Transparent,
    Turquoise,
    Violet,
    Wheat,
    White,
    WhiteSmoke,
    Yellow,
    YellowGreen)
    var actual = -1
    
    def color: paint.Color = {
      actual = (actual + 1) % colors.length
      colors(actual)
    }
  }
  
  private var colors: HashMap[Company, paint.Color] = new HashMap()
  colors(company) = Red
  world.companies.filter { _ != company }.map { colors(_) = ColorManager.color }
  
  /* Actual content, inside a ZoomPane.
     The ScrollPane is only a container. */
  private object MapContent extends ZoomPane with Drawable {
    /** The dots representing a train on route on the map.
     *
     *  The `Circle` object is associated with the corresponding `Travel`
     *  to allow automatic updating of its position.
     */
    private class MapTravel(val travel: Travel) extends Circle {
      radius = 8
      fill = colors(travel.company)
      onMouseClicked = new EventHandler[MouseEvent] {
        override def handle(event: MouseEvent) {
          displayTravel(travel)
        }
      }

      /** Updates circle position. */
      def draw(): Unit = {
        // Coordonates of start and end towns
        val x1 = travel.currentRoute.start.x
        val y1 = travel.currentRoute.start.y
        val x2 = travel.currentRoute.end.x
        val y2 = travel.currentRoute.end.y
        val p = travel.currentRouteProportion
        // train coordonates
        centerX = x1 * (1-p) + x2 * p
        centerY = y1 * (1-p) + y2 * p
      }
    }

    // the list of current travels
    private var travels: List[MapTravel] = List()

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
      travels = new MapTravel(travel) :: travels
    }

    world.onAddTravel = addTravel

    /** Update dynamic map. */
    override def draw(): Unit = {
      // remove finished travels
      travels = travels.filter(!_.travel.isDone)
      // update all positions
      travels.foreach(_.draw())
      // update content
      dynamicMap.children = travels
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
