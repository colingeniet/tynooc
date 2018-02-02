class Town(name: String, xPos: Double, yPos: Double)
extends PositionnedVertice {
  class Route(_destination: Town, _length: Double) {
    val destination: Town = _destination
    val length: Double = _length
  }

  val x: Double = xPos
  val y: Double = yPos
  private var _routes: List[Route] = List()

  def routes: List[Route] = _routes
  def addRoute(newRoute: Route): Unit = {
    _routes = newRoute::_routes
  }
  def iterateNeighbours(action: Vertice=>Unit): Unit = {
    this.routes.foreach((route: Route) => action(route.destination))
  }
}
