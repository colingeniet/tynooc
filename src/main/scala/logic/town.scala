package logic.town

import logic.graph._
import logic.route._
import logic.game._
import logic.world._

import collection.mutable.HashMap
import java.util.Random


/** A town in the world. */
class Town(
  val name: String,
  val x: Double,
  val y: Double,
  val welcomingLevel: Double) {
  private var residents: Array[Int] = new Array(Game.world.statusNumber)
  private var _routes: List[Route] = List()
  private var passengers: HashMap[Town, Array[Int]] = new HashMap()

  // PRNG
  private var random: Random = new Random()

  /** The routes starting from this town. */
  def routes: List[Route] = _routes
  /** The neighbours towns. */
  def neighbours: List[Town] = routes.map { _.end }
  /** The town population. */
  def population: Int = residents.sum
  def passengersNumber: Int = passengers.values.map(_.sum).sum
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

  def deletePassengers(nb: Int, status: Status.Val, destination: Town): Unit = {
    if(nb > passengers(destination)(status.id))
      throw new IllegalArgumentException("population should stay positive")
    passengers(destination)(status.id) -= nb
    deleteResidents(nb, status)
  }

  def generateMigrant(to: Town, dt: Double): Int = {
    val pop = population
    val pass = passengersNumber
    // avoid accumulating too many passengers
    // slow down passengers production when proportion increases,
    // and hard cap it at 1/4 of total population
    val coef = 0.02 * (1 - 4 * pass / pop)
    val mean = (pop - pass) * (1 + to.note - note) * coef * dt
    // it's not like deviance is really different
    (((random.nextGaussian() * mean + mean) max 0) min pop).toInt
  }

  /** Adds a new route. */
  def addRoute(route: Route): Unit = {
    if(route.start != this)
      throw new IllegalArgumentException("route should start from $name town")
    _routes = route :: _routes
    // Add new entry to passengers map
    if(!passengers.contains(route.end)) {
      passengers(route.end) = Array.ofDim(Game.world.statusNumber)
    }
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
      val migrantNumber = generateMigrant(destination, dt)
      val byStatus = {
        if(p == 0)
          residents
        else
          residents.map { r => (migrantNumber * r.toDouble / p).floor.toInt }
      }
      passengers(destination) = (passengers(destination), byStatus).zipped.map(_ + _)

      Game.world.tryTravel(this, destination, passengers(destination))
    }
  }

  override def toString: String = {
    s"$name: $population residents, $welcomingLevel welcoming level, $note note at ($x, $y)."
  }
}
