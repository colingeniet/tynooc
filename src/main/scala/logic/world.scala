package logic.world

import logic.town._
import logic.travel._
import logic.room._
import logic.company._
import logic.route._
import logic.vehicle._
import logic.good._

import collection.mutable.HashMap
import collection.mutable.HashSet

import scala.math.Ordering.Implicits._



/** The game world representation.
 *
 *  The world is a graph, with vertices being towns, and edges being routes.
 *  This class handles all travels, as well as town population.
 *
 * @constructor Creates an empty world with its height and its width.
 */
class World {
  /** The fuel price in the world. */
  var fuelPrice = 1

  private var _towns: HashSet[Town] = HashSet()

  private var _travels: HashSet[Travel] = HashSet()
  private var _companies: HashSet[Company] = HashSet()

  /** Callback called any time a new travel is added.
    *
    *  This is used to signal the new travel to the gui.
    */
  var onAddTravel: Travel => Unit = {_ => ()}

  /** The towns in the world. */
  def towns: HashSet[Town] = _towns

  /** The current travels in the world. */
  def travels: HashSet[Travel] = _travels

  /** The train companies in the world. */
  def companies: HashSet[Company] = _companies

  /** Total world population. */
  var population: Int = 0

  /** Adds a new town.
    *
    * @param town The town to add.
    */
  def addTown(town: Town): Unit = {
    _towns.add(town)
    population += town.population()
  }

  /** Creates and add a new town.
    *
    * @param name The name of the town.
    * @param x the x position of the town.
    * @param y the y position of the town.
    * @param welcomingLevel The welcoming level of the town (between 0 and 1).
    */
  def addTown(name: String, x: Double, y: Double, welcomingLevel: Double): Unit = {
    addTown(new Town(name, x, y, welcomingLevel))
  }

  /** Adds a new company to the world
    *
    * @param company The new company.
    */
  def addCompany(company: Company): Unit = {
    _companies.add(company)
  }

  /** Adds a new travel in the world.
    *
    * @param travel The travel to add.
    */
  def addTravel(travel:Travel): Unit = {
    _travels.add(travel)
    // callback
    onAddTravel(travel)
  }

  /** Gets all travels of a specific company in the world.
    *
    * @param company The company.
    */
  def travelsOf(company: Company): HashSet[Travel] =
    travels.filter { _.company == company }


  /** Try to send some goods from <code>start</code> to
    * <code>destination</code>.
    *
    * @param start The start town.
    * @param destination The destination town.
    * @param good The goods to send.
    * @param quantity The quantity of goods to send.
    */
  def tryExport(
    start: Town,
    destination: Town,
    good: Good,
    quantity: Double): Unit = {
    val availableTravels = travels.toList.filter {
      t => t.isWaitingAt(start) && t.stopsAt(destination)
    }
    var rooms = availableTravels.flatMap { _.availableRooms }
    var remaining = quantity

    while(remaining > 0 && !rooms.isEmpty) {
      val room = rooms.head
      val q: Double = remaining min room.availableLoad(good)
      room.load(good, destination, q)
      start.buyGoods(room.travel.company, good, q)
      remaining -= q
      rooms = rooms.tail
    }
  }

  /** Update the state of all travels and towns.
   *
   *  @param dt the time passed since last update step.
   */
  def update(dt: Double): Unit = {
    travels.foreach(_.update(dt))
    _travels = travels.filter(!_.isDone())
    towns.foreach(_.update(dt))
  }

  def update_towns(): Unit = {
    val totalGoods: HashMap[Good, Double] = HashMap()
    Good.all.foreach { g =>
      totalGoods(g) = towns.map(_.goods(g)()).sum
    }

    towns.foreach(_.update_economy(totalGoods))
  }

  /** Find the shortest path between two towns.
   *
   * @param from start town.
   * @param to   end town.
   * @return The list of vertices in the path, in order.
   */
  def findPath(from: Town, to: Town, vehicle: Vehicle) : Option[List[Route]] = {
    // Dijkstra
    val closed: HashSet[Town] = new HashSet()
    val open: HashSet[Town] = new HashSet()
    val dist: HashMap[Town, Double] = new HashMap()
    val path: HashMap[Town, List[Route]] = new HashMap()
    dist(from) = 0
    open.add(from)
    path(from) = List()

    while(!open.isEmpty && !closed(to)) {
      val town: Town = open.minBy[Double](dist(_))
      town.routes.filter(_.accepts(vehicle)).foreach { route =>
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

  /** Returns the list of towns accessibles from a starting town.
    *
    * @param from The town.
    */
  def townsAccessibleFrom(from: Town, vehicle: Vehicle): List[Town] = {
    vehicle match {
      case p: Plane => { towns.toList.filter(_.accepts(vehicle)) }
      case _        => {
        val closed: HashSet[Town] = new HashSet()
        val open: HashSet[Town] = new HashSet()
        open.add(from)

        while (!open.isEmpty) {
          val town = open.head

          town.routes.filter(_.accepts(vehicle)).foreach { route =>
            if(!open(route.end) && !closed(route.end)) {
              open += (route.end)
            }
          }
          open.remove(town)
          closed.add(town)
        }
        closed.toList.filter(_.accepts(vehicle))
      }
    }
  }
}
