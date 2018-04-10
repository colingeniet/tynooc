package logic.room

import scalafx.beans.property._

import logic.vehicle._
import logic.travel._
import logic.game._
import logic.world._
import logic.town._
import logic.good._

import collection.mutable.HashMap
import collection.mutable.HashSet

/** A room in a train.
  *
  * @constructor Creates a room with a travel and a vehicle unit.
  * @param travel The travel in which this room is present.
  * @param vehicle The vehicle unit associated to the room.
 */
class Room(val travel: Travel, val vehicle: VehicleUnit) {
  val passengers: HashMap[Town, Int] = new HashMap[Town, Int] {
    override def default(t: Town): Int = {
      this(t) = 0
      this(t)
    }
  }

  val contents: HashMap[Town, HashMap[Good, Double]] =
    new HashMap[Town, HashMap[Good, Double]] {
      override def default(t: Town): HashMap[Good, Double] = {
        this(t) = new HashMap[Good, Double] {
          override def default(g: Good): Double = {
            this(g) = 0
            this(g)
          }
        }
        this(t)
      }
    }

  private var filled: Double = 0

  /** Maximum number of passengers in the room. */
  def capacity: Int = vehicle.capacity
  /** The comfort note of the room (between 0 and 1). */
  def comfort: Double = vehicle.comfort
  /** Goods capacity. */
  def allowed: HashMap[Good, Double] = vehicle.allowed


  /** Number of passengers in the room. */
  def passengerNumber: Int = passengers.values.sum

  def goods(g: Good): Double = contents.values.map(_(g)).sum

  /** Returns <code>true</code> if some places are available in the room. */
  def isAvailable: Boolean = passengerNumber < capacity
  /** Number of available places in the room. */
  def availablePlaces: Int = capacity - passengerNumber

  /** The price of a place for <code>destination</code>.
    *
    * @param destination The town where you want to go.
    */
  def price(destination: Town): Double = {
    0.20 * travel.remainingDistanceTo(destination)
  }

  /** Free the places of <code>number</code> passengers of status </status>
    * going to <code>destination</code>
    *
    * @param number The number of passenger who free their places.
    * @param destination The destination of these passengers.
    */
  def freePlaces(destination: Town, number: Int): Unit = {
    assert(number <= passengers(destination))
    passengers(destination) -= number
  }

  /** Free the places of all passengers of status `status` going to
    * <code>destination</code>
    *
    * @param destination The destination of these passengers.
    */
  def freePlaces(destination: Town): Unit = {
    freePlaces(destination, passengers(destination))
  }

  /** Take <code>number</code> places for passengers of status <code>status</code>
      going to <code>destination</code>
    *
    * @param number The number of passenger who take places.
    * @param destination The destination of these passengers.
    * @param status The status of these passengers.
    */
  def takePlaces(destination: Town, number: Int): Unit = {
    assert(number <= availablePlaces)
    passengers(destination) += number
    travel.company.credit(price(destination) * number)
  }

  /** Get passengers on to every place you're going to go to
  * @param town The place where you're taking your passengers
  */
  def embarkAll(town: Town): Unit = {
    travel.remainingStops.foreach(dest => {
      val n = availablePlaces min (town.passengers(dest).floor.toInt)
      takePlaces(dest, n)
      town.deletePassengers(dest, n)
    })
  }

  /** Checks if a certain good can be allowed
  * @param g The good in question
  */
  def availableLoad(g: Good): Double = (1 - filled) * allowed(g)


  /** Loads a certain quantity of a good
  * @param g The good to load on
  * @param destination The place where you load the good
  * @param v The quantity to load
  */
  def load(g: Good, destination: Town, v: Double): Unit = {
    assert(v <= (1 - filled) * allowed(g))

    contents(destination)(g) += v
    filled += v/allowed(g)
  }

  /** Unloads a certain quantity of a good
  * @param g The good to unload
  * @param destination The place where you unload the good
  * @param v The quantity to unload
  */
  def unload(g: Good, destination: Town, v: Double): Unit = {
    assert(v <= contents(destination)(g))

    contents(destination)(g) -= v
    filled -= v/allowed(g) max 0
    destination.sellGoods(travel.company, g, v)
  }

  /** Unloads a certain good
  * @param g The good to unload
  * @param destination The place where you unload the good
  */
  def unload(g: Good, destination: Town): Unit = {
    if(allowed(g) > 0) {
      unload(g, destination, contents(destination)(g))
    }
  }

  /** Try to load goods of every type
  * @param town The town where you pick up the goods
  */
  def loadAll(town: Town): Unit = {
    travel.remainingStops.foreach(dest => {
      Good.all.foreach(g => {
        val quantity = availableLoad(g) min town.toExport(dest)(g) min town.goods(g)()
        if (quantity > 0) {
          load(g, dest, quantity)
          town.buyGoods(travel.company, g, quantity)
          town.toExport(dest)(g) = town.toExport(dest)(g) - quantity
        }
      })
    })
  }

  /** Unload everything a room carries.
  * @param destination The town where you unload your content
  */
  def unloadAll(destination: Town): Unit = {
    Good.all.foreach(unload(_, destination))
  }

  /** Update every goods.
  * @param dt Time since last update
  */
  def handleGoods(dt: Double) : Unit = {
    contents.values.foreach(_.foreach{case (g, v) => if (v > 0) g.update(this, dt)})
  }
}
