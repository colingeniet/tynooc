package logic.town

import scalafx.beans.binding._
import scalafx.beans.property._
import scalafx.collections._

import logic.route._
import logic.game._
import logic.world._
import logic.good._
import logic.facility._
import logic.vehicle._
import logic.company._
import utils.InitHashMap

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
  private var _routes: List[Route] = List()

  /** The town population. */
  val population: IntegerProperty = IntegerProperty(0)
  /** The passengers number of the town. */
  val passengers: HashMap[Town, Double] = InitHashMap[Town, Double](_ => 0)

  val passengersNumber: DoubleProperty = DoubleProperty(0)


  //Gives the quantity of good inside the city
  val goods: HashMap[Good, DoubleProperty] =
    InitHashMap[Good, DoubleProperty](_ => DoubleProperty(0))

  // Gives the prices of goods in this city
  val goods_prices: HashMap[Good, DoubleProperty] =
    InitHashMap[Good, DoubleProperty](g => DoubleProperty(g.basePrice))

  // Stocks at last tick. Used to estimate consumption
  private val goods_last: HashMap[Good, Double] =
    InitHashMap[Good, Double](_ => 0)


  val toExport: HashMap[Town, HashMap[Good, Double]] =
    InitHashMap[Town, HashMap[Good, Double]](_ => {
      InitHashMap[Good, Double](_ => 0)
    })


  // Coeff used for consumation
  val consume_coeffs: HashMap[Good, Double] = {
    val a = InitHashMap[Good, Double](_ => 0)
    Good.setAnyWith[CityNeeded](a, 0.7d/1000)
    Good.setAnyWith[Consumable](a, 1d/1000)
    a
  }


  val facilities: ObservableBuffer[Facility] = ObservableBuffer()


  /** Returns a hashmap containing the needs of the population
  */
  def needs() : HashMap[Good, Double] = {
    val a: HashMap[Good, Double] = HashMap()
    Good.all.foreach{ g =>
      if(population()*consume_coeffs(g) <= goods(g)())
        a(g) = population.toDouble * consume_coeffs(g)
      else {
        a(g) = goods(g)()
        Game.printMessage(s"Good Lord! The people of ${name} are severly lacking ${g.name}!")
        //Do something related to happiness
      }
    }
    a
  }

  def sellGoods(company: Company, g: Good, v: Double): Unit = {
    company.credit(goods_prices(g)() * v)
    goods(g)() = goods(g)() + v
  }

  def sellGoods(company: Company, h: HashMap[Good, Double]): Unit = {
    h.foreach{ case (g, v) => sellGoods(company, g, v) }
  }

  /** Consume goods of every type every time it's called.
  */
  def consume_daily(): Unit = {
    val a = needs()
    goods.foreach{ case (key, value) => goods(key)() = value() - a(key) }
  }

  def available(g: Good, v: Double): Boolean = {
    goods(g)() >= v
  }

  /** Test if goods are available.
   */
  def available(h: HashMap[Good, Double]): Boolean = {
    h.forall{ case (g, v) => available(g, v) }
  }


  def buyGoods(company: Company, g: Good, v: Double): Boolean = {
    if (available(g, v) && company.money() >= goods_prices(g)() * v) {
      company.debit(goods_prices(g)() * v)
      goods(g)() = goods(g)() - v
      true
    } else false
  }

  /** Consume a certain quantity of good, if available.
   *
   *  @param h The goods to consume associated with the quantities
   *  @return true on success, false if goods are unavailable
   */
  def buyGoods(company: Company, h: HashMap[Good, Double]): Boolean = {
    if (available(h)) {
      h.foreach{ case (g, v) => buyGoods(company, g, v) }
      true
    } else false
  }


  /** Calculates the prices of goods
  */
  def update_prices(totals: HashMap[Good, Double]) : Unit = {
    Good.all.foreach{ g =>
      val local = goods(g)()
      val total = totals(g)
      val avg = total / Game.world.towns.size

      val world_coef = (((avg + 1.0) / (local + 1.0)) max 0.2) min 4.0

      val pop_required = population.toDouble * consume_coeffs(g)
      val pop_needs_coef = (1.0 / ((((local + 2.0) / (pop_required + 1.0)) - 1.0) max 0.1)) max 1.0

      val consumed = goods_last(g) - local
      val needs_coef = 1.0 / ((((local + 2.0) / (consumed + 1.0)) - 1.0) max 0.2)

      goods_prices(g)() = g.basePrice * world_coef * needs_coef * pop_needs_coef
      goods_last(g) = local
    }
  }


  /** The routes starting from this town. */
  def routes: List[Route] = _routes
  /** The neighbours towns. */

  def neighbours: List[Town] = routes.map { _.end }

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
    */
  def addResidents(number: Int): Unit = {
    population() = population() + number
    Game.world.population += number
  }

  /** Deletes <code>number</code residents of status <code>status</code>
    * to the town.
    *
    * @param number The number of residents to delete.
    *
    */
  def deleteResidents(number: Int): Unit = {
    assert(number <= population())
    population() = population() - number
    Game.world.population -= number
  }

  /** Deletes <code>number</code passengers of status <code>status</code>
    * to the town.
    *
    * @param number The number of passengers to delete.
    */
  def deletePassengers(destination: Town, number: Int): Unit = {
    assert(number <= passengers(destination))
    passengers(destination) -= number
    passengersNumber() = passengersNumber() - number
    deleteResidents(number)
  }

  /** Generate passengers to a town.
    *
    * @param to The destination town.
    * @param dt The time passed since the last generation.
    */
  def generatePassengers(to: Town, dt: Double): Double = {
    val pop = population.toDouble
    if (pop == 0) return 0

    val pass = passengersNumber.toDouble
    /* avoid accumulating too many passengers :
     * slow down passengers production when proportion increases,
     * and hard cap it at 1/4 of total population */
    val coef = 0.0001 * (1 - 4 * pass / pop)

    (pop - pass) * (1 + to.note - note) * coef * dt
  }

  def generateExports(to: Town, g: Good, dt: Double): Double = {
    goods(g)() * 0.2 * ((((to.goods_prices(g)() / goods_prices(g)()) - 1) max 0) min 2)
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


  def accepts(v: Vehicle): Boolean = {
    v match {
      case _: Truck => true
      case _ => facilities.exists( _ match {
        case s: Station => s.accepts(v)
        case _ => false
      })
    }
  }


  def update_economy(mostDemanding: HashMap[Good, List[Town]]) : Unit = {
    val p = population()
    Game.world.towns.foreach { destination =>
      if(destination != this) {
        val migrantNumber = generatePassengers(destination, Game.economyTick)
        passengersNumber() = passengersNumber() + migrantNumber
        passengers(destination) += migrantNumber
      }
    }
    Good.all.foreach { g =>
      mostDemanding(g).foreach { destination =>
        if(destination != this) {
          val quantity = generateExports(destination, g, Game.economyTick)
          toExport(destination)(g) += quantity
        }
      }
    }

    consume_daily()
  }

  /** Update the population state.
   *
   *  @param dt The time passed since the last update step.
   */
  def update(dt: Double): Unit = {
    facilities.foreach(_ match {
      case f: Factory => if(!f.working()) f.startCycle()
      case _ => ()
    })
  }
}
