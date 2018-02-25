package logic.room

import logic.train._
import logic.player._
import logic.world._
import logic.travel._

final case class CantBuy(private val message: String = "", 
                         private val cause: Throwable = None.orNull)
                         extends Exception(message, cause)

final case class CantFree(private val message: String = "", 
                         private val cause: Throwable = None.orNull)
                         extends Exception(message, cause)
                         
class Room(val price: Int, val travel: Travel, val carriage: Carriage) {

  private var _passengers: Array[Array[Int]] = Array.ofDim(Game.world.statusNumber,
                                                           Game.world.townNumber)
  
  def passengers: Array[Array[[Int]] = _passengers
  def passengerNumber: Int = (passengers.map { _.sum}).sum
  def capacity: Int = carriage.capacity
  def isAvailable: Boolean = passengerNumber < capacity
  def availablePlaces: Int = capacity - passengerNumber
  def comfort: Double = carriage.comfort
  
  def statusToTownNumber(destination: Town, status: Status.val): Int = {
    passengers[status.id][destination]
  }
  
  def freePlaces(nb: Int, destination:Town, status: Status.val): Unit = {
    throw new CantFree if nb > passengers[status.id][destination]
    passengers[status.id][destination] -= nb 
  } 
  
  def takePlaces(nb: Int, destination: Town, status: Status.val):Unit = {
    throw new CantBuy if nb > availablePlaces
    passengers[status.id][destination] += nb
    travel.owner.addMoney(price * nb)
  }
}
