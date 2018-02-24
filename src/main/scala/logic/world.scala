package logic.world

import logic.graph._
import logic.game._
import logic.PNJ._
import logic.player._


/** World representation
 */
class World extends Graph {
  
  private var _towns: List[World.Town] = List()
  private var _travels: List[Travel] = List()
  private var _pnjs: List[PNJ] = List()
  private var _population: Int = 50
  /** The list of towns.
   */
  def towns: List[World.Town] = _towns

  /** The list of current travels.
   */
  def travels: List[Travel] = _travels

  /** The list of pnjs.
   */
  def pnjs: List[PNJ] = _pnjs

  /** The population
   */
  def population: Int = _population

  /** Adds a town.
   *
   *  @param newTown the town to add.
   */
  def addTown(newTown: World.Town): Unit = {
    _towns = newTown :: _towns
  }

  /** Add Pnjs to a Town
    *
    * @param p the PNJ to add
    */
  def addPNJ(p: PNJ): Unit = {
    p.town.population += 1
    _population += p.number
    _pnjs = p :: _pnjs
  }

  def vertices: List[World.Town] = towns

  /** Update the world
  *
  *   @param dt the delta time between two calls.
  */
  def update(dt: Double) =
  {
    pnjs.foreach {p:PNJ => p.update(dt)}
  }
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
  class Town(name: String, xPos: Double, yPos: Double, w: Double)
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

    def incidentEdges: List[Route] = routes

    def neighbours: List[Town] = {
      routes.map { _.destination }
    }

    def note: Double = {
      /* Formulas to find */
      (welcomingLevel*population)/Game.world.population
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

  def real_to_virtual_time(t: Double) : Double = {
    return 50*t
  }

  def virtual_to_real_time(t: Double) : Double = {
    return t/50
  }
}
