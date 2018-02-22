package world

import graph._

/** World representation
 */
class World extends Graph {
  private var _towns: List[World.Town] = List()

  /** The list of towns.
   */
  def towns: List[World.Town] = _towns

  /** Adds a town.
   *
   *  @param newTown the town to add.
   */
  def addTown(newTown: World.Town): Unit = {
    _towns = newTown :: _towns
  }

  def vertices: List[World.Town] = towns
}

/** World object companion
 */
object World {
  /** A town in the world.
   *
   *  @constructor creates a town in the `World`.
   *  @param name the town name.
   *  @param xPos the town x coordonate.
   *  @param yPos the town y coordonate.
   */
  class Town(_name: String, xPos: Double, yPos: Double, w: Double)
  extends Graph.Vertice {
    /** The town x coordonate in the world. */
    val x: Double = xPos
    /** The town y coordonate in the world. */
    val y: Double = yPos
    /* The welcoming level of a town, between 0 and 1 */
    val welcomingLevel: Double = w

    private var _routes: List[Route] = List()

    var population: Int = 0

    /** The list of routes.
     */
    def routes: List[Route] = _routes

    /** Adds a route.
     *
     *  @param newRoute the route to add.
     */
    def addRoute(newRoute: Route): Unit = {
      _routes = newRoute :: _routes
    }
    /** Creates a route and add it.
     *
     *  The new route starts from `this`.
     *  @param to the route destination.
     *  @param length the route length.
     */
    def addRoute(to: Town, length: Double): Unit = {
      addRoute(new Route(this, to, length))
    }

    def incidentEdges: List[Route] = routes

    def name: String = _name

    def neighbours: List[Town] = {
      routes.map(r => r.destination)
    }

    def note: Double = {
      /* Formulas to find */
      welcomingLevel / population
    }
  }

  /** A route starting from this town.
   *
   *  @constructor creates a route to another `Town`.
   *  @param _destination the route destination town.
   *  @param _length the route length.
   */
  class Route(from: Town, to: Town, _length: Double) extends Graph.Edge {
    val start: Town = from
    val end: Town = to
    val weight: Double = _length

    /** The route destination. */
    def destination: Town = end

    /** The route length. */
    def length: Double = weight
  }
}
