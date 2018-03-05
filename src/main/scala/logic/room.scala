package logic.room

import logic.train._
import logic.travel._
import logic.game._
import logic.world._
import logic.town._

import collection.mutable.HashMap
import collection.mutable.HashSet

/** An exception which could be throwed if a places in a room
    can’t be bought.
  */
final case class CantBuy(
  private val message: String = "",
  private val cause: Throwable = None.orNull)
extends Exception(message, cause)

/** An exception which could be throwed if a places in a room
    can’t be released.
  */
final case class CantFree(
  private val message: String = "",
  private val cause: Throwable = None.orNull)
extends Exception(message, cause)

/** A room in a train.
  *
  * @constructor Creates a room with a travel and a carriage.
  * @param travel The travel in which this room is present.
  * @param carriage The carriage associated to the room.
 */
class Room(val travel: Travel, val carriage: Carriage) {
  private var passengers: HashMap[Town, Array[Int]] = new HashMap()
  Game.world.towns.foreach(passengers(_) = Array.ofDim[Int](Game.world.statusNumber))

  /** Number of passengers in the room. */
  def passengerNumber: Int = passengers.values.map(_.sum).sum
  /** Maximum number of passengers in the room. */
  def capacity: Int = carriage.capacity
  /** Returns <code>true</code> if some places are available in the room. */
  def isAvailable: Boolean = passengerNumber < capacity
  /** Number of available places in the room. */
  def availablePlaces: Int = capacity - passengerNumber
  /** The comfort note of the room (betwween 0 and 1). */
  def comfort: Double = carriage.comfort

  /** The price of a place for <code>destination</code>.
    *
    * @param destination The town where you want to go.
    */
  def price(destination: Town): Double = {
    carriage.placePrice * travel.remainingDistanceTo(destination)
  }

  /** Number of passengers which status <code>status</code> going to
    * <code>destination</code>.
    *
    * @param status The status of the counted passengers.
    * @param destination The destination of the counted passengers.
    */
  def passengerNumber(status: Status.Val, destination: Town): Int = {
    passengers(destination)(status.id)
  }

  /** Number of passengers in the room going to <code>destination</code>.
    *
    * @param destination The destination of the counted passengers.
    */
  def passengerNumber(destination: Town): Int = passengers(destination).sum

  /** Free the places of <code>number</code> passengers of status </status>
    * going to <code>destination</code>
    *
    * @param number The number of passenger who free their places.
    * @param destination The destination of these passengers.
    * @param status The status of these passengers.
    * @throws CantFree exception if it have to free more places than
    *         taken places.
    */
  def freePlaces(number: Int, destination: Town, status: Status.Val): Unit = {
    if(number > passengers(destination)(status.id))
      throw new CantFree
    passengers(destination)(status.id) -= number
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
    * @throws CantBuy exception if there are not enough available places.
    */
  def takePlaces(number: Int, destination: Town, status: Status.Val): Unit = {
    if(number > availablePlaces)
      throw new CantBuy
    passengers(destination)(status.id) += number
    travel.owner.credit(price(destination) * number)
  }
}
