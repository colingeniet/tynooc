package logic.town

import scalafx.beans.binding._
import scalafx.beans.property._
import scalafx.collections._

import logic.route._
import logic.game._
import logic.world._
import logic.good._
import logic.facility._

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
  private var passengers: HashMap[Town, HashMap[Status.Val, Double]] =
    new HashMap[Town, HashMap[Status.Val, Double]] {
      override def default(t: Town): HashMap[Status.Val, Double] = {
        // initialize empty entries
        this(t) = new HashMap[Status.Val, Double]()
        Game.world.status.foreach { s => this(t)(s) = 0 }
        this(t)
      }
    }

  Game.world.status.foreach { s => residents(s) = 0 }

  //Gives the quantity of good inside the city
  val goods: HashMap[Good, DoubleProperty] =
    new HashMap[Good, DoubleProperty] {
      override def default(g: Good): DoubleProperty = {
        // initialize empty entries
        this(g) = DoubleProperty(0)
        this(g)
      }
    }

  //Coeff used for consumation
  val consume_coeffs: HashMap[Good, Double] = HashMap()

  //Gives the prices of goods in this city
  val goods_prices: HashMap[Good, DoubleProperty] = HashMap()

  // PRNG
  private var random: Random = new Random()


  val facilities: ObservableBuffer[Facility] = ObservableBuffer()

  /** Returns a hashmap containing the needs of the population
  */
  def needs() : HashMap[Good, Double] = {
      val a: HashMap[Good, Double] = HashMap()
      Good.all.foreach{ g =>
        if(population()*consume_coeffs(g) < goods(g)())
          a(g) = population()*consume_coeffs(g)
        else {
          a(g) = goods(g)()
          Game.printMessage(s"Good Lord! The people of ${name} are severly lacking of ${g.name} !")
          //Do something related to happiness
        }
      }
      a
  }

  def addGoods(g: Good, v: Double): Unit = {
    goods(g)() = goods(g)() + v
  }

  def addGoods(h: HashMap[Good, Double]): Unit = {
    h.foreach{ case (g, v) => goods(g)() = goods(g)() + v }
  }

  /** Calculates the prices of goods
  */
  def price_update() : Unit = {
  }

  /** Consume goods of every type every time it's called.
  */
  def consume_daily: Unit = {
    val a = needs()
    goods.foreach{ case (key, value) => goods(key)() = value() - a(key) }
  }

  /** Test if goods are available.
   */
  def available(h: HashMap[Good, Double]): Boolean = {
    h.forall{ case (g, v) => goods(g)() > v }
  }

  /** Consume a certain quantity of good, if available.
   *
   *  @param h The goods to consume associated with the quantities
   *  @return true on success, false if goods are unavailable
   */
  def consume(h: HashMap[Good, Double]): Boolean = {
    if (available(h)) {
      h.foreach{ case (g, q) => goods(g)() = goods(g)() - q }
      true
    }
    else false
  }


  /** The routes starting from this town. */
  def routes: List[Route] = _routes
  /** The neighbours towns. */

  def neighbours: List[Town] = routes.map { _.end }
  /** The town population. */
  val population: IntegerProperty = IntegerProperty(0)
  /** The passengers number of the town. */
  val passengersNumber: DoubleProperty = DoubleProperty(0)

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
    assert(number <= residents(status))
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
    assert(number <= passengers(destination)(status))
    passengers(destination)(status) -= number
    passengersNumber() = passengersNumber() - number
    deleteResidents(number, status)
  }

  /** Generate passengers to a town.
    *
    * @param to The destination town.
    * @param dt The time passed since the last generation.
    */
  def generatePassengers(to: Town, dt: Double, s: Status.Val): Double = {
    val pop = population.toDouble
    if (pop == 0) return 0

    val pass = passengersNumber.toDouble
    /* avoid accumulating too many passengers :
     * slow down passengers production when proportion increases,
     * and hard cap it at 1/4 of total population */
    val coef = 0.0001 * (1 - 4 * pass / pop) * residents(s) / pop
    // mean for gaussian approximation
    val mean = (pop - pass) * (1 + to.note - note) * coef * dt
    // With this coef value, deviance is barely different from the mean
    (((random.nextGaussian() * mean + mean) max 0) min pop)
  }

  /** Adds a new route.
    *
    * @param route The route to add to the town.
    */
  def addRoute(route: Route): Unit = {
    assert(route.start == this)
    _routes = route :: _routes
  }


  def addFacility(f: Facility): Unit = {
    assert(f.town == this)
    facilities.add(f)
  }


  /** Update the population state.
   *
   *  @param dt The time passed since the last update step.
   */
  def update(dt: Double): Unit = {
    val p = population()
    val possibleDestinations = Game.world.towns.toList.sortBy { _.note }
    possibleDestinations.foreach { destination =>
      Game.world.status.foreach { s =>
        val migrantNumber = generatePassengers(destination, dt, s)
        passengersNumber() = passengersNumber() + migrantNumber
        passengers(destination)(s) += migrantNumber
      }
      Game.world.tryTravel(this, destination, passengers(destination))
    }


    facilities.foreach(_ match {
      case f: Factory => if(!f.working()) f.startCycle()
      case _ => ()
    })
  }
}
