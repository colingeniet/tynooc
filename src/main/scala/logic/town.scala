package logic.town

import logic.graph._
import logic.route._
import logic.game._
import logic.world._

private object TownId {
  private var id = 0
  def nextId: Int = { id += 1; id }
}

class Town(
  val name: String,
  val x: Double,
  val y: Double,
  val welcomingLevel: Double)
extends Graph.Vertice {
  val id = TownId.nextId
  private var residents: Array[Int] = new Array(Game.world.statusNumber)
  private var _routes: List[Route] = List()

  def routes: List[Route] = _routes
  def incidentEdges: List[Route] = routes
  def neighbours: List[Town] = routes.map { _.end }
  def population: Int = residents.sum
  def note: Double =
    /* + 1 to avoid division by zero */
    welcomingLevel * (1 + population) / (1 + Game.world.population)

  def addResidents(nb: Int, status: Status.Val): Unit =
    residents(status.id) += nb

  def deleteResidents(nb: Int, status: Status.Val): Unit = {
    if(nb > residents(status.id))
      throw new IllegalArgumentException("population should stay positive")
    residents(status.id) -= nb
  }

  def generateMigrant(to: Town): Int =
    Math.max(0, population * (to.note - note)).toInt


  def addRoute(route: Route): Unit = {
    /* Check if route.start == this */
    _routes = route :: _routes
  }

  def addRoute(end: Town, length: Double): Unit = {
    _routes = (new Route(this, end, length)) :: _routes
  }

  def update(dt: Double): Unit = {
    val p = population
    val possibleDestinations = neighbours.sortBy { _.note }
    possibleDestinations.foreach { destination =>
      val migrantNumber = generateMigrant(destination)
      val byStatus = residents.map { r => (migrantNumber * r / p).toInt }
      Game.world.tryTravel(this, destination, byStatus)
    }
  }

  override def toString: String = {
    s"$name: $population residents, $welcomingLevel welcoming level, $note note at ($x, $y)."
  }
}
