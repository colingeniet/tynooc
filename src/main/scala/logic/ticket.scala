package logic.ticket

import logic.train._
import logic.player._

/*
* Exception thrown if you try to buy a Ticket that is not available
*/
final case class CantBuy(private val message: String = "", 
                           private val cause: Throwable = None.orNull)
                      extends Exception(message, cause)

/*
* Represent a set of tickets
*/
class Ticket(p: Int) {

  val travel: Travel = null
  val price: Int = 5
  val carriage: Carriage = null
  var amount: Int = p
  var comfort: Double = carriage.model.comfort

  /* Return if you can buy a place or not
  *
  */
  def available: Boolean = { (p > 0) }

  /* Throw an exception if the place canot be bought
  */
  def buy: Unit = {

    if(amount > 0)
    {
      throw new CantBuy
    }

    else
    {
      amount-=1
      travel.owner.addMoney(price)
    }
  }

  def free: Unit = {
    amount += 1
  }
}
