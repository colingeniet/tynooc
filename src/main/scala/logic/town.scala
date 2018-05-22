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
import logic.mission._
import utils.InitHashMap

import collection.mutable.HashMap
import scala.util.Random
import java.io._


/** A town in the world.
  *
  * @constructor Creates a town with its name, its position and its welcomingLevel
  * @param name The town’s name.
  * @param x The x position of the town.
  * @param y The y position of the town.
  * @param welcomingLevel The welcoming level of a town (between 0 and 1).
  */
@SerialVersionUID(0L)
class Town(
  val name: String,
  val x: Double,
  val y: Double,
  val welcomingLevel: Double)
extends Serializable {
  private var _routes: List[Route] = List()

  /** The town population. */
  @transient var population: IntegerProperty = IntegerProperty(0)
  /** The passengers number of the town. */
  val passengers: HashMap[Town, Double] = InitHashMap[Town, Double](_ => 0)

  @transient var passengersNumber: DoubleProperty = DoubleProperty(0)


  //Gives the quantity of good inside the city
  @transient var goods: HashMap[Good, DoubleProperty] =
    InitHashMap[Good, DoubleProperty](g => DoubleProperty(0))

  // Gives the prices of goods in this city
  @transient var goods_prices: HashMap[Good, DoubleProperty] =
    InitHashMap[Good, DoubleProperty](g => DoubleProperty(g.basePrice))

  // Stocks at last tick. Used to estimate consumption
  private val goods_last: HashMap[Good, Double] =
    InitHashMap[Good, Double](_ => 0)

  // Goods to be exported
  val toExport: HashMap[Good, Double] = InitHashMap[Good, Double](_ => 0)

  var requestsTime: HashMap[Good, Int] = InitHashMap[Good, Int](_ => 0)


  def needs(g: Good): Double = population() * consume_coeffs(g)
  // Coeff used for consumation
  val consume_coeffs: HashMap[Good, Double] = {
    val a = InitHashMap[Good, Double](_ => 0)
    Good.setAnyWith[CityNeeded](a, 0.7d/1000)
    Good.setAnyWith[Consumable](a, 1d/1000)
    Good.setAnyWith[Elec](a, 1d/840)
    a
  }


  @transient var facilities: ObservableBuffer[Facility] = ObservableBuffer()

  def addGood(g: Good, q: Double) = {
    val t = q / 4
    goods(g)() += (q - t)
    toExport(g) += t
    if(needs(g) > goods(g)()) { requestsTime(g) = 0 }
  }

  def deleteGood(g: Good, q: Double) = goods(g)() -= q

  def exportGood(g: Good, q: Double) = toExport(g) -= q
  /** Tries to buy a good from a certain company
  * @param company The company the town will buy the good from
  * @param v The quantity corresponding.
  */
  def sellGoods(company: Company, g: Good, v: Double): Unit = {
    company.credit(goods_prices(g)() * v)
    goods(g)() = goods(g)() + v
  }

  /** Tries to buy goods from a certain company
  * @param company The company the town will buy it's goods from
  * @param h The (hashmap) set of goods to sell and the quantity corresponding.
  */
  def sellGoods(company: Company, h: HashMap[Good, Double]): Unit = {
    h.foreach{ case (g, v) => sellGoods(company, g, v) }
  }
                                                     /** Sell goods of the town
  * @param company The company the town will sell goods to
  * @param g The good it sells
  * @param v The quantity of the good
  */
  def buyGoods(company: Company, g: Good, v: Double): Boolean = {
    if (available(g, v) && company.money() >= goods_prices(g)() * v) {
      company.debit(goods_prices(g)() * v)
      goods(g)() = goods(g)() - v
      true
    } else false
  }

  /** Sell a certain quantity of good, if available.
    * @param company The company the town sells to
   *  @param h The goods to consume associated with the quantities
   *  @return true on success, false if goods are unavailable
   */
  def buyGoods(company: Company, h: HashMap[Good, Double]): Boolean = {
    if (available(h)) {
      h.foreach{ case (g, v) => buyGoods(company, g, v) }
      true
    } else false
  }
  /** Consume goods of every type every time it's called.
  * It is called every economy economy tick.
  * (~3h when i'm writing this)
  */
  def consume_daily(): Unit = {
    goods.foreach{ case (key, value) => goods(key)() = (value() - needs(key)) max 0 }
  }

  /** Says if a certain quantity of a certain good is available
  * @param g The good in question
  * @param v The quantity of this good
  */
  def available(g: Good, v: Double): Boolean = {
    goods(g)() >= v
  }

  /** Test if goods are available.
  * @param h The set of goods & quantity you want to check the availability of
  */
  def available(h: HashMap[Good, Double]): Boolean = {
    h.forall{ case (g, v) => available(g, v) }
  }




  /** Recalculates the prices of goods
  * @param totals The totals of each good in the world
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


  /** Adds a new route.
    *
    * @param route The route to add to the town.
    */
  def addRoute(route: Route): Unit = {
    assert(route.start == this)
    _routes = route :: _routes
  }

  /** Adds a new facility.
    *
    * @param f The facility to add to the town.
    */
  def addFacility(f: Facility): Unit = {
    assert(f.town == this)
    facilities.add(f)
  }

  /** Tests if a vehicle can stop at this town.
  * @param v The vehicle to test
  */
  def accepts(v: Vehicle): Boolean = {
    v match {
      case _: Truck => true
      case _: Tank => true
      case _ => facilities.exists( _ match {
        case s: Station => s.accepts(v)
        case _ => false
      })
    }
  }

  /** Stations that can be used by a vehicle.
  * @param v The vehicle in question
  */
  def stationsFor(v: Vehicle): Set[Station] = {
    facilities.filter {
      case s: Station => s.accepts(v)
      case _          => false
    }.toSet.asInstanceOf[Set[Station]]
  }


  /** Economic tick : update passengers, exportations, consumption
  * @param mostDemanding The towns that needs goods the most
  */
  def update_economy(mostDemanding: HashMap[Good, List[Town]]) : Unit = {
    val p = population()
    Game.world.towns.foreach { destination =>
      if(destination != this) {
        val migrantNumber = generatePassengers(destination, Game.economyTick)
        passengersNumber() = passengersNumber() + migrantNumber
        passengers(destination) += migrantNumber
      }
    }
    val requestedGoods = Good.all.filter{ g =>  needs(g) > goods(g)() }
    requestedGoods.foreach { g => requestsTime(g) += 1 }
    requestedGoods.filter(requestsTime(_) > 3).toList.sortBy(-requestsTime(_))
    requestedGoods.take(5).foreach { g =>
      val dealers = Game.world.searchDealers(this, g)
      if(!dealers.isEmpty) {
        val d = Random.shuffle(dealers).head
        requestsTime(g) -= 3
        val q = needs(g) * 2
        val mission_reward = q * 3
        val mission = new HelpMission(mission_reward, d, this, Game.time() + 24, g, q)
        Game.world.sendMission(mission)
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


  @throws(classOf[IOException])
  private def writeObject(stream: ObjectOutputStream): Unit = {
    stream.defaultWriteObject()
    stream.writeObject(this.population.toInt)
    stream.writeObject(this.passengersNumber.toDouble)
    stream.writeObject(this.goods.map{ case(g,v) => (g,v.toDouble) })
    stream.writeObject(this.goods_prices.map{ case(g,v) => (g,v.toDouble) })
    stream.writeObject(this.facilities.toList)
  }

  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  private def readObject(stream: ObjectInputStream): Unit = {
    stream.defaultReadObject()
    this.population = IntegerProperty(stream.readObject().asInstanceOf[Int])
    this.passengersNumber = DoubleProperty(stream.readObject().asInstanceOf[Double])

    this.goods = InitHashMap[Good, DoubleProperty](g => DoubleProperty(0))
    val new_goods = stream.readObject().asInstanceOf[HashMap[Good,Double]]
    new_goods.foreach{ case (g,v) => this.goods(g)() = v }

    this.goods_prices = InitHashMap[Good, DoubleProperty](g => DoubleProperty(g.basePrice))
    val new_goods_prices = stream.readObject().asInstanceOf[HashMap[Good,Double]]
    new_goods_prices.foreach{ case (g,v) => this.goods_prices(g)() = v }

    this.facilities = ObservableBuffer[Facility](stream.readObject().asInstanceOf[List[Facility]])
  }
}
