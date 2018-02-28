package logic.world

import logic.graph._
import logic.town._
import logic.travel._
import logic.room._
import logic.player._
import logic.route._

import collection.mutable.HashMap
import collection.mutable.HashSet

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

class World {
  var status = Array(Status.RICH, Status.POOR, Status.WELL)
  var statusCriteria = Array((r:Room) => r.comfort, (r:Room) => r.price,
                             (r:Room) => r.comfort / (r.price+1))
  var statusNumber = status.length
  var townNumber = 0

  private var _towns: List[Town] = List()
  private var _travels: List[Travel] = List()

  def towns: List[Town] = _towns
  def travels: List[Travel] = _travels
  def population: Int = towns.foldLeft[Int](0) { _ + _.population }

  def addTown(town: Town): Unit = { _towns = town :: _towns; townNumber += 1}

  def addTown(name: String, x: Double, y: Double, welcomingLevel: Double): Unit = {
    _towns = new Town(name, x, y, welcomingLevel) :: _towns
    townNumber += 1
  }

  def addTravel(travel:Travel): Unit = _travels = travel :: _travels

  def travelsOf(player: Player) = travels.filter { _.owner == player }

  def tryTravel(start:Town, destination:Town, migrantByStatus:Array[Int]):Unit = {
    val availableTravels = travels.filter { t => t.isWaitingAt(start) &&
                                                 t.stopsAt(destination) }
    var rooms = availableTravels.flatMap { _.availableRooms }
    status.foreach { status =>
      var takenPlacesNumber = 0
      val p = migrantByStatus(status.id)
      rooms = rooms.sortBy { statusCriteria(status.id) }
      do {
        val room = rooms.head
        val nb = Math.max(p, room.availablePlaces)
        room.takePlaces(nb, destination, status)
        takenPlacesNumber += nb
        if(!room.isAvailable)
          rooms = rooms.tail
      } while(takenPlacesNumber < p && !rooms.isEmpty)
      start.deleteResidents(takenPlacesNumber, status)
    }
  }

  def update(dt: Double): Unit = {
    travels.foreach { _.update(dt) }
    _travels = travels.filter { !_.isDone}
    // add later
    //towns.foreach { _.update(dt) }
  }

  /** Find the shortest path between two towns.
   *
   * @param from start town.
   * @param to   end town.
   * @return The list of vertices in the path, in order.
   */
  def findPath(from: Town, to: Town) : Option[List[Route]] = {
    // Dijkstra
    var closed: HashSet[Town] = new HashSet()
    var open: HashSet[Town] = new HashSet()
    var dist: HashMap[Town, Double] = new HashMap()
    var path: HashMap[Town, List[Route]] = new HashMap()
    dist(from) = 0
    open.add(from)
    path(from) = List()

    while(!open.isEmpty && !closed(to)) {
      var town: Town = open.minBy[Double](dist(_))
      town.routes.foreach { route =>
        if(!open(route.end) && !closed(route.end)) {
          open.add(route.end)
          dist(route.end) = dist(town) + route.length
          path(route.end) = route :: path(town)
        } else if(open(route.end) && dist(route.end) > dist(town) + route.length) {
          dist(route.end) = dist(town) + route.length
          path(route.end) = route :: path(town)
        }
      }
      open.remove(town)
      closed.add(town)
    }

    // path was build in reverse direction
    path.get(to) match {
      case None => None
      case Some(l) => Some(l.reverse)
    }
  }

  def townsAccessibleFrom(from: Town): List[Town] = {
    var closed: Set[Town] = Set()
    var open: Set[Town] = Set(from)
    var accessibles: Set[Town] = Set()
    while (!open.isEmpty) {
      val town = open.head
      closed += town
      open -= town
      town.neighbours.foreach { n =>
        if(!closed.contains(n)) {
          open = open + n 
          accessibles += n; 
        }
      }
    }
    accessibles.toList
  }
  
  override def toString: String = {
    towns.foldLeft[String]("") { (d, t) => d + s"$t\n" }
  }
}

object World {
  def realToVirtualTime(t: Double) : Double = t
  def virtualToRealTime(t: Double) : Double = t
}
