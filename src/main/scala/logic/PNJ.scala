package logic.PNJ

import logic.game._
import logic.player._
import logic.ticket._
import logic.world._

object State {
  sealed trait Val
  case object WAITING extends Val
  case object ON_ROAD extends Val
  case object SETTLED extends Val   
}
  
object Status {
  sealed trait Val
  case object RICH extends Val
  case object POOR extends Val
  case object WELL extends Val   
}

class PNJ(s: Status.Val, st : State.Val, t : World.Town, p : Double) {
  val status: Status.Val = s
  var state: State.Val = st
  var town: World.Town = t
  var destination: World.Town = t
  var move_probability: Double = p 
  var ticket: Ticket = null
  val number: Int = 10
  
  def want_to_migrate: Boolean = {
    return town.note < move_probability       
  }
  
  def try_migration(): Unit = {
  	if(want_to_migrate) {
  	  search_destination()
  	  state = State.WAITING
  	}
  }

  def search_destination(): Unit = {
    destination = town.neighbours.maxBy(_.note)
  }
  
  def search_travel(): Unit = {
    val travels = Game.world.travels.filter { _.is_waiting }
    if(travels.isEmpty)
      return
    val tickets = travels.flatMap { _.available_tickets }
    ticket = status match {
      case Status.RICH => tickets.maxBy { _.comfort }
      case Status.POOR => tickets.minBy { _.price }
      case Status.WELL => tickets.head
    }
    ticket.buy
    state = State.ON_ROAD
    town.population -= 1
  }
  
  def travel(): Unit = {
    if(ticket.travel.is_arrived && ticket.travel.town == destination) {
      ticket.free
      town = destination
      town.population += 1
      state = State.SETTLED
    }
    else {
      /* Consumption when it will be implemented */
    }    
  }  
  
  def update(dt : Double): Unit = {
    state match {
      case State.WAITING => search_travel() 
      case State.SETTLED => try_migration()
      case State.ON_ROAD => ()/* Check if he want to consume */
    }
  }  
}
