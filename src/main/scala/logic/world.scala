package logic.world

import scalafx.application.Platform

import logic.town._
import logic.travel._
import logic.room._
import logic.company._
import logic.route._
import logic.vehicle._
import logic.good._
import logic.mission._
import logic.game._
import player._

import collection.mutable.HashMap
import collection.mutable.HashSet

import scala.math.Ordering.Implicits._
import scala.util.Random
import java.io._

/** The game world representation.
 *
 *  The world is a graph, with vertices being towns, and edges being routes.
 *  This class handles all travels, as well as town population.
 *
 * @constructor Creates an empty world with its height and its width.
 */
@SerialVersionUID(0L)
class World extends Serializable {
  /** The fuel price in the world. */
  var fuelPrice = 0.05

  var towns: HashSet[Town] = HashSet()

  var travels: HashSet[Travel] = HashSet()
  var companies: HashSet[Company] = HashSet()

  var minX: Double = 0
  var minY: Double = 0
  var maxX: Double = 0
  var maxY: Double = 0

  /** Callback called any time a new travel is added.
    *
    *  This is used to signal the new travel to the gui.
    */
  @transient var onAddTravel: Travel => Unit = {_ => ()}

  /** Total world population. */
  var population: Int = 0

  def generateMissionCompanyCandidate(m : Mission) : Player = {

    val p = m match {
      case (_ : HelpMission) => 0.8
      case (_ : FretMission) => 0.5
    }

    val v = Random.nextInt()
    if (v >= p) {
      val h = Random.nextInt(Game.players.length)
      return Game.players(h)
    }
    else
     Game.bigBrother
  }

  //Should be called every X tick
  def sendMission(m : Mission) : Unit = {
    generateMissionCompanyCandidate(m).company.addWaitingMission(m)
  }

  /** Adds a new town.
    *
    * @param town The town to add.
    */
  def addTown(town: Town): Unit = {
    towns.add(town)
    population += town.population()
    minX = minX min town.x
    maxX = maxX max town.x
    minY = minY min town.y
    maxY = maxY max town.y
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
    companies.add(company)
  }

  /** Adds a new travel in the world.
    *
    * @param travel The travel to add.
    */
  def addTravel(travel:Travel): Unit = {
    travels.add(travel)
    // callback
    onAddTravel(travel)
  }

  /** Gets all travels of a specific company in the world.
    *
    * @param company The company.
    */
  def travelsOf(company: Company): HashSet[Travel] =
    travels.filter { _.company == company }


  /** Update the state of all travels and towns.
   *
   *  @param dt the time passed since last update step.
   */
  def update(dt: Double): Unit = {
    travels.foreach(_.update(dt))
    travels = travels.filter(!_.isDone())
    towns.foreach(_.update(dt))
  }

  def update_towns(): Unit = {
    val totalGoods: HashMap[Good, Double] = HashMap()
    Good.all.foreach { g =>
      totalGoods(g) = towns.map(_.goods(g)()).sum
    }

    towns.foreach(_.update_prices(totalGoods))

    // only export to the most demanding towns for each good.
    // This is done to keep calculation to an acceptable level:
    // exporting to all towns results in a quadratic complexity, which
    // is too much on big maps
    val mostDemanding: HashMap[Good, List[Town]] = HashMap()
    Good.all.foreach { g =>
      mostDemanding(g) = towns.toList.sortWith((t1, t2) => t1.goods_prices(g)() > t2.goods_prices(g)()).slice(0,5)
    }

    towns.foreach(_.update_economy(mostDemanding))
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


  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  private def readObject(stream: ObjectInputStream): Unit = {
    stream.defaultReadObject()
    onAddTravel = {_ => ()}
  }
}
