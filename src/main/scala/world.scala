/** A route starting from this town.
 *
 *  @constructor creates a route to another `Town`.
 *  @param _destination the route destination town.
 *  @param _length the route length.
 */
class Route(_destination: Town, _length: Double) {
  /** The route destination. */
  val destination: Town = _destination
  /** The route length. */
  val length: Double = _length
}

/** A town in the world.
 *
 *  @constructor creates a town in the `World`.
 *  @param name the town name.
 *  @param xPos the town x coordonate.
 *  @param yPos the town y coordonate.
 */
class Town(name: String, xPos: Double, yPos: Double, w: Double)
extends PositionWeightedVertice {

  /** The town x coordonate in the world. */
  val x: Double = xPos
  /** The town y coordonate in the world. */
  val y: Double = yPos
  /* The welcoming level of a town, between 0 and 1 */
  val welcomingLevel: Double = w

  private var _routes: List[Route] = List()

  var population: Int = 0
  
  /** The list of routes. */
  def routes: List[Route] = _routes
  /** Adds a route.
   *
   *  @param newRoute the route to add.
   */
  def addRoute(newRoute: Route): Unit = {
    _routes = newRoute::_routes
  }

  /** Iterates over all adjacent edges.
   *
   *  @param action the function called for each edge.
   *  Takes the destination Vertice and the edge weight as parameters.
   */
  def iterateWeightedEdges(action: (Vertice, Double)=>Unit): Unit = {
    this.routes.foreach((route: Route) => action(route.destination, route.length))
  }
  
  def neighbours: List[Town] = {
    routes.map(r => r.destination) 
  }
  
  def note: Double = {
    /* Formulas to find */
    welcomingLevel / population
  }  
}

/** World representation */
class World extends PositionWeightedGraph[Double] {

  private var _towns: List[Town] = List()

  /** The list of towns. */
  def towns: List[Town] = _towns
  /** Adds a town.
   *
   *  @param newTown the town to add.
   */
  def addTown(newTown: Town): Unit = {
    _towns = newTown::_towns
  }
}
