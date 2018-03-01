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
  case object Launched extends Val
  case object Waiting extends Val //Waiting for passengers
  case object OnRoute extends Val
  case object Arrived extends Val //Passengers are leaving now
}

class Travel(val train: Train, private val roads: List[Route],
             val owner: Player) {

  if(!owner.ownsTrain(train))
    throw new IllegalArgumentException("Player doesn’t own the train")

  private val rooms: List[Room] = train.carriages.map { new Room(this, _) }
  private val distance: Double = (roads.map { _.length }).sum
  private var currentRouteDistanceDone: Double = 0
  private var remainingRoutes: List[Route] = roads
  private var state: State.Val = State.Launched
  /* private var currentRouteIndex = 0 */

  def nextTown: Town = currentRoute.end
  def destination: Town = roads.last.end
  def currentRoute: Route = remainingRoutes.head
  def currentTown: Town = train.town
  def isDone: Boolean = remainingRoutes.isEmpty
  def stopsAt(t: Town): Boolean = (remainingRoutes.map { _.end}).contains(t)

  def totalRemainingDistance: Double =
    (remainingRoutes.map { _.length }).sum - currentRouteDistanceDone
  def remainingDistance: Double = currentRoute.length - currentRouteDistanceDone

  def totalRemainingTime: Double = totalRemainingDistance / train.engine.speed
  def remainingTime: Double = remainingDistance / train.engine.speed

  def currentRouteProportion: Double = currentRouteDistanceDone / currentRoute.length

  def passengerNumber: Int = (rooms.map { _.passengerNumber}).sum

  def isWaiting: Boolean = state == State.Waiting
  def isLaunched: Boolean = state == State.Launched
  def isOnRoute: Boolean = state == State.OnRoute
  def isArrived: Boolean = state == State.Arrived
  def isWaitingAt(town: Town): Boolean = (isWaiting || isLaunched) && currentTown == town

  def availableRooms: List[Room] = rooms.filter { _.isAvailable }

  def landPassengers: Unit = {
    rooms.foreach { room =>
      Game.world.status.foreach { status =>
        currentTown.addResidents(room.passengerNumber(status, currentTown), status)
        room.freePlaces(currentTown, status)
      }
    }
  }

  def update(dt: Double): Unit = {
    if(!isDone) {
      state match {
        case State.Launched => state = State.Waiting
        case State.OnRoute => {
          currentRouteDistanceDone += dt * train.engine.speed
          if(currentRouteDistanceDone >= currentRoute.length) {
            state = State.Arrived
          }
        }
        case State.Arrived => {
          train.deteriorate(currentRoute)
          state = State.Waiting
          landPassengers
          remainingRoutes = remainingRoutes.tail
          currentRouteDistanceDone = 0
          if(isDone) train.travel = None
        }
        case State.Waiting => {
          train.town = nextTown
          state = State.OnRoute
        }
      }
    }
  }
}
