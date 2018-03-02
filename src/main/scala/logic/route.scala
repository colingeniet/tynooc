package logic.route

import logic.graph._
import logic.town._

/** A route between two towns. */
class Route(val start: Town, val end: Town, val length: Double, val damageToVehicle: Double)
