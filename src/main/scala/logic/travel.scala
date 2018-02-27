package logic.travel

import logic.train._
import logic.world._
import logic.town._
import logic.room._
import logic.route._
import logic.player._
import logic.game._

object State {
  sealed trait Val
  case object Waiting extends Val //Waiting for passengers
  case object OnRoute extends Val
  case object Arrived extends Val //Passengers are leaving now
}

class Travel(val train: Train, private val roads: List[Route],
             val owner: Player, private val rooms : List[Room]) {

  if(!owner.owns(train))
    throw new IllegalArgumentException("Player doesn’t own the train")

  private val distance: Double = (roads.map { _.length }).sum
  private var currentRouteDistanceDone: Double = 0
  private var remainingRoutes: List[Route] = roads
  private var currentRoute: Route = remainingRoutes.head
  private var currentTown: Town = currentRoute.start
  private var state: State.Val = State.Waiting
  /* private var currentRouteIndex = 0 */

  def next_step: Town = currentRoute.end
  def destination: Town = roads.last.end
  def town: Town = currentTown
  def isDone: Boolean = remainingRoutes.isEmpty

  def totalRemainingDistance: Double =
    (remainingRoutes.map { _.length }).sum - currentRouteDistanceDone
  def remainingDistance: Double = currentRoute.length - currentRouteDistanceDone
  def remainingTime: Double = remainingDistance / train.engine.speed

  def passengerNumber: Int = (rooms.map { _.passengerNumber}).sum

  def isWaiting: Boolean = state == State.Waiting
  def isOnRoute: Boolean = state == State.OnRoute
  def isArrived: Boolean = state == State.Arrived

  def availableRooms: List[Room] = rooms.filter { _.isAvailable }

  def landPassengers: Unit = {
    rooms.foreach { room =>
      Game.world.status.foreach { status =>
        currentTown.addResidents(room.passengers(status.id)(currentTown.id), status)
        room.freePlaces(room.passengers(status.id)(currentTown.id), currentTown, status)
      }
    }
  }

  def update(dt: Double): Unit = {
    if(isDone)
      return
    state match {
      case State.OnRoute => {
        currentRouteDistanceDone += World.realToVirtualTime(dt) * train.engine.speed
        if(currentRouteDistanceDone >= currentRoute.length) {
          state = State.Arrived
          currentRouteDistanceDone = 0
        }
      }
      case State.Arrived => {
        train.deteriorate(currentRoute)
        state = State.Waiting
        remainingRoutes = remainingRoutes.tail
        /* currentRouteIndex += 1 */
        currentRoute = remainingRoutes.head
      }
      case State.Waiting => {
        state = State.OnRoute
        currentTown = currentRoute.destination
      }
    }
  }
}
