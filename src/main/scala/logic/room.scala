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
    Array.ofDim(Game.world.statusNumber, Game.world.townNumber)

  def passengers: Array[Array[Int]] = _passengers
  def passengerNumber: Int = passengers.foldLeft[Int](0) { _ + _.sum }
  def capacity: Int = carriage.capacity
  def isAvailable: Boolean = passengerNumber < capacity
  def availablePlaces: Int = capacity - passengerNumber
  def comfort: Double = carriage.comfort

  def statusToTownNumber(destination: Town, status: Status.Val): Int = {
    passengers(status.id)(destination.id)
  }

  def freePlaces(nb: Int, destination: Town, status: Status.Val): Unit = {
    if(nb > passengers(status.id)(destination.id))
      throw new CantFree
    passengers(status.id)(destination.id) -= nb
  }

  def takePlaces(nb: Int, destination: Town, status: Status.Val): Unit = {
    if(nb > availablePlaces)
      throw new CantBuy
    passengers(status.id)(destination.id) += nb
    travel.owner.addMoney(price * nb)
  }
}
