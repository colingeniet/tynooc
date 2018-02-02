trait Position {
  val x: Double
  val y: Double
}

class Town(name: String, x_pos: Double, y_pos: Double) extends Position {
  class Route(destination: Town, length: Double)

  val x:Double = x_pos
  val y:Double = y_pos
  private var _routes:List[Route] = List()

  def routes:List[Route] = _routes
  def add_route(newRoute: Route): Unit = {
    _routes = newRoute::_routes
  }
}
