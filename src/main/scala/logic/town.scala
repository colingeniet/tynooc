package logic.town

import scalafx.beans.binding._
import scalafx.beans.property._

import logic.route._
import logic.game._
import logic.world._

import collection.mutable.HashMap
import java.util.Random


/** A town in the world.
  *
  * @constructor Creates a town with its name, its position and its welcomingLevel
  * @param name The town’s name.
  * @param x The x position of the town.
  * @param y The y position of the town.
  * @param welcomingLevel The welcoming level of a town (between 0 and 1).
  */
class Town(
  val name: String,
  val x: Double,
  val y: Double,
  val welcomingLevel: Double) {
  private var residents: HashMap[Status.Val, Int] = new HashMap()
  private var _routes: List[Route] = List()
  private var passengers: HashMap[Town, HashMap[Status.Val, Int]] = new HashMap()
  Game.world.status.foreach { s => residents(s) = 0 }
  // PRNG
  private var random: Random = new Random()

  /** The routes starting from this town. */
  def routes: List[Route] = _routes
  /** The neighbours towns. */

  def neighbours: List[Town] = routes.map { _.end }
  /** The town population. */
  val population: IntegerProperty = IntegerProperty(0)
  /** The passengers number of the town. */
  val passengersNumber: IntegerProperty = IntegerProperty(0)

  /** The note of the town. */
  def note: Double = {
    if(population == 0)
      1
    else
      welcomingLevel * (1 - population.toDouble / Game.world.population)
  }

  /** Adds <code>number</code residents of status <code>status</code>
    * to the town.
    *
    * @param number The number of residents to add.
    * @param status The status of these residents.
    */
  def addResidents(number: Int, status: Status.Val): Unit = {
    residents(status) += number
    population() = population() + number
  }

  /** Deletes <code>number</code residents of status <code>status</code>
    * to the town.
    *
    * @param number The number of residents to delete.
    * @param status The status of these residents.
    *
    */
  def deleteResidents(number: Int, status: Status.Val): Unit = {
    if(number > residents(status))
      throw new IllegalArgumentException("population should stay positive")
    residents(status) -= number
    population() = population() - number
  }

  /** Deletes <code>number</code passengers of status <code>status</code>
    * to the town.
    *
    * @param number The number of passengers to delete.
    * @param status The status of these passengers.
    */
  def deletePassengers(number: Int, status: Status.Val, destination: Town): Unit = {
    if(number > passengers(destination)(status))
      throw new IllegalArgumentException("population should stay positive")
    passengers(destination)(status) -= number
    passengersNumber() = passengersNumber() - number
    deleteResidents(number, status)
  }

  /** Generate passengers to a town.
    *
    * @param to The destination town.
    * @param dt The time passed since the last generation.
    */
  def generatePassengers(to: Town, dt: Double, s: Status.Val): Int = {
    val pop = population.toDouble
    if (pop == 0) return 0

    val pass = passengersNumber.toDouble
    /* avoid accumulating too many passengers :
     * slow down passengers production when proportion increases,
     * and hard cap it at 1/4 of total population */
    val coef = 0.01 * (1 - 4 * pass / pop) * residents(s) / pop
    // mean for gaussian approximation
    val mean = (pop - pass) * (1 + to.note - note) * coef * dt
    // With this coef value, deviance is barely different from the mean
    (((random.nextGaussian() * mean + mean) max 0) min pop).toInt
  }

  /** Adds a new route.
    *
    * @param route The route to add to the town.
    */
  def addRoute(route: Route): Unit = {
    if(route.start != this)
      throw new IllegalArgumentException("route should start from $name town")
    _routes = route :: _routes
    // Add new entry to passengers map
    if(!passengers.contains(route.end)) {
      passengers(route.end) = new HashMap()
      Game.world.status.foreach { s => passengers(route.end)(s) = 0 }
    }
  }

  /** Update the population state.
   *
   *  @param dt The time passed since the last update step.
   */
  def update(dt: Double): Unit = {
    val p = population()
    val possibleDestinations = neighbours.sortBy { _.note }
    possibleDestinations.foreach { destination =>
      Game.world.status.foreach { s =>
        val migrantNumber = generatePassengers(destination, dt, s)
        passengersNumber() = passengersNumber() + migrantNumber
        passengers(destination)(s) += migrantNumber
      }
      Game.world.tryTravel(this, destination, passengers(destination))
    }
  }
}
