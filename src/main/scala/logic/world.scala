package logic.world

import logic.graph._
import logic.game._
import logic.player._
import logic.route._
import logic.town._

object Status {
  var _id = 0
  sealed class Val(val id: Int) {
    _id += 1
    def this() { this(_id) } 
  } 
  object RICH extends Status.Val
  object POOR extends Status.Val
  object WELL extends Status.Val 
}

/** World representation
 */
class World extends Graph {
  
  var statusNumber = 3
  var status = List(Status.RICH, Status.POOR, Status.WELL)
  
  private var _towns: List[World.Town] = List()
  private var _travels: List[Travel] = List()
  private var _population: Int = 50
  /** The list of towns.
   */
  def towns: List[World.Town] = _towns

  def tryTravel(start:Town, destination:Town, migrantByStatus:Array[Int]):Unit = {
  
  }
  /** The list of current travels.
   */
  def travels: List[Travel] = _travels


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

  def vertices: List[World.Town] = towns

  /** Update the world
  *
  *   @param dt the delta time between two calls.
  */
  def update(dt: Double): Unit =
  {
   
  }
}

/** World object companion
 */
object World {

  def real_to_virtual_time(t: Double) : Double = {
    return 50*t
  }

  def virtual_to_real_time(t: Double) : Double = {
    return t/50
  }
}
