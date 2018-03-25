package logic.route

import logic.graph._
import logic.town._

/** A route between two towns.
  *
  * @constructor Creates a route with its ends town, its lenght
  * @param start The town at the beginning of the route.
  * @param end The town at the end of the route.
  * @param length The lenght of the route.
  */
class Route(val start: Town, val end: Town, val length: Double)
