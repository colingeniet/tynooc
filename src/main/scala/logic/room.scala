package logic.room

import logic.train._
import logic.travel._
import logic.game._
import logic.world._
import logic.town._


final case class CantBuy(
  private val message: String = "",
  private val cause: Throwable = None.orNull)
extends Exception(message, cause)

final case class CantFree(
  private val message: String = "",
  private val cause: Throwable = None.orNull)
extends Exception(message, cause)


class Room(val travel: Travel, val carriage: Carriage) {
  private var _passengers: Array[Array[Int]] =
    Array.ofDim(Game.world.townNumber, Game.world.statusNumber)

  def passengers: Array[Array[Int]] = _passengers
  def passengerNumber: Int = passengers.foldLeft[Int](0) { _ + _.sum }
  def capacity: Int = carriage.capacity
  def isAvailable: Boolean = passengerNumber < capacity
  def availablePlaces: Int = capacity - passengerNumber
  def comfort: Double = carriage.comfort
  def price: Double = carriage.placePrice
  
  def passengerNumber(status: Status.Val, destination: Town): Int = {
    passengers(destination.id)(status.id)
  }
  
  def passengerNumber(destination: Town): Int = {
    passengers(destination.id).sum
  } 

  def freePlaces(nb: Int, destination: Town, status: Status.Val): Unit = {
    if(nb > passengers(destination.id)(status.id))
      throw new CantFree
    passengers(destination.id)(status.id) -= nb
  }

  def freePlaces(destination: Town, status: Status.Val): Unit = {
    Game.world.status.foreach { status =>
      freePlaces(passengerNumber(status, destination), destination, status)
    }
  }
 
  def takePlaces(nb: Int, destination: Town, status: Status.Val): Unit = {
    if(nb > availablePlaces)
      throw new CantBuy
    passengers(destination.id)(status.id) += nb
    travel.owner.addMoney(price * nb)
  }

}
