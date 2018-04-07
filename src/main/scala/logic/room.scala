package logic.room

import logic.vehicle._
import logic.travel._
import logic.game._
import logic.world._
import logic.town._

import collection.mutable.HashMap
import collection.mutable.HashSet

/** A room in a train.
  *
  * @constructor Creates a room with a travel and a vehicle unit.
  * @param travel The travel in which this room is present.
  * @param vehicle The vehicle unit associated to the room.
 */
class Room(val travel: Travel, val vehicle: VehicleUnit) {
  private var passengers: HashMap[Town, HashMap[Status.Val, Int]] = new HashMap()
  Game.world.towns.foreach { t =>
    passengers(t) = new HashMap()
    Game.world.status.foreach { s =>
      passengers(t)(s) = 0
    }
  }
  /** Number of passengers in the room. */
  def passengerNumber: Int = passengers.values.flatMap(_.values).sum
  /** Maximum number of passengers in the room. */
  def capacity: Int = vehicle match {
    case c: Carriage => c.capacity
    case p: Plane => p.capacity
    case _ => 0
  }
  /** Returns <code>true</code> if some places are available in the room. */
  def isAvailable: Boolean = passengerNumber < capacity
  /** Number of available places in the room. */
  def availablePlaces: Int = capacity - passengerNumber
  /** The comfort note of the room (betwween 0 and 1). */
  def comfort: Double = vehicle match {
    case c: Carriage => c.comfort
    case p: Plane => p.comfort
    case v => 1
  }

  /** The price of a place for <code>destination</code>.
    *
    * @param destination The town where you want to go.
    */
  def price(destination: Town): Double = {
    0.20 * travel.remainingDistanceTo(destination)
  }

  /** Number of passengers which status <code>status</code> going to
    * <code>destination</code>.
    *
    * @param status The status of the counted passengers.
    * @param destination The destination of the counted passengers.
    */
  def passengerNumber(status: Status.Val, destination: Town): Int = {
    passengers(destination)(status)
  }

  /** Number of passengers in the room going to <code>destination</code>.
    *
    * @param destination The destination of the counted passengers.
    */
  def passengerNumber(destination: Town): Int = passengers(destination).values.sum

  /** Free the places of <code>number</code> passengers of status </status>
    * going to <code>destination</code>
    *
    * @param number The number of passenger who free their places.
    * @param destination The destination of these passengers.
    * @param status The status of these passengers.
    */
  def freePlaces(number: Int, destination: Town, status: Status.Val): Unit = {
    assert(number <= passengers(destination)(status))
    passengers(destination)(status) -= number
  }

  /** Free the places of all passengers of status `status` going to
    * <code>destination</code>
    *
    * @param destination The destination of these passengers.
    * @param status The status of these passengers.
    */
  def freePlaces(destination: Town, status: Status.Val): Unit = {
    Game.world.status.foreach { status =>
      freePlaces(passengerNumber(status, destination), destination, status)
    }
  }

  /** Take <code>number</code> places for passengers of status <code>status</code>
      going to <code>destination</code>
    *
    * @param number The number of passenger who take places.
    * @param destination The destination of these passengers.
    * @param status The status of these passengers.
    */
  def takePlaces(number: Int, destination: Town, status: Status.Val): Unit = {
    assert(number <= availablePlaces)
    passengers(destination)(status) += number
    travel.company.credit(price(destination) * number)
  }
}
