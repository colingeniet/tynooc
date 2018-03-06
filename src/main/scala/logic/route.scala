package logic.route

import logic.graph._
import logic.town._

/** A route between two towns. 
  *
  * @constructor Creates a route with its ends town, its lenght, and the damageToVehicle
                 it inflicts to a train.
  * @param start The town at the beginning of the route.
  * @param end The town at the end of the route.
  * @param length The lenght of the route.
  * @param The damage points it inflicts.  
  */
class Route(val start: Town, val end: Town, val length: Double, val damageToVehicle: Double)
