package logic.town

import logic.graph._
import logic.route._
import logic.game._
import logic.world._

private object TownId {
  private var id = 0
  def nextId: Int = { id += 1; id }
}

/** A town in the world. */
class Town(
  val name: String,
  val x: Double,
  val y: Double,
  val welcomingLevel: Double) {
  val id = TownId.nextId
  private var residents: Array[Int] = new Array(Game.world.statusNumber)
  private var _routes: List[Route] = List()

  /** The routes starting from this town. */
  def routes: List[Route] = _routes
  /** The neighbours towns. */
  def neighbours: List[Town] = routes.map { _.end }
  /** The town population. */
  def population: Int = residents.sum
  def note: Double = {
    if(population == 0)
      1
    else
      welcomingLevel * (1 - population.toDouble / Game.world.population)
  }

  def addResidents(nb: Int, status: Status.Val): Unit =
    residents(status.id) += nb

  def deleteResidents(nb: Int, status: Status.Val): Unit = {
    if(nb > residents(status.id))
      throw new IllegalArgumentException("population should stay positive")
    residents(status.id) -= nb
  }

  def generateMigrant(to: Town): Int =
    Math.max(0, population * (to.note - note)).toInt

  /** Adds a new route. */
  def addRoute(route: Route): Unit = {
    if(route.start != this)
      throw new IllegalArgumentException("route should start from $name town")
    _routes = route :: _routes
  }

  /** Creates and adds a new route to a town. */
  def addRoute(end: Town, length: Double, state: Double): Unit = {
    _routes = (new Route(this, end, length, state)) :: _routes
  }

  /** Update the population state.
   *
   *  @param dt the time passed since the last update step.
   */
  def update(dt: Double): Unit = {
    val p = population
    val possibleDestinations = neighbours.sortBy { _.note }
    possibleDestinations.foreach { destination =>
      val migrantNumber = generateMigrant(destination)
      val byStatus = {
        if(p == 0)
          residents
        else
          residents.map { r => (migrantNumber * r.toDouble / p).floor.toInt }
      }
      Game.world.tryTravel(this, destination, byStatus)
    }
  }

  override def toString: String = {
    s"$name: $population residents, $welcomingLevel welcoming level, $note note at ($x, $y)."
  }
}
