package logic.travel

import logic.train._
import logic.world._
import logic.town._
import logic.room._
import logic.route._
import logic.company._
import logic.game._

object State {
  sealed trait Val
  case object Launched extends Val
  case object Waiting extends Val //Waiting for passengers
  case object OnRoute extends Val
  case object Arrived extends Val //Passengers are leaving now
}

/** A travel. */
class Travel(val train: Train, private val roads: List[Route],
             val owner: Company) {

  if(!owner.ownsTrain(train))
    throw new IllegalArgumentException("Company doesn’t own the train")

  private val rooms: List[Room] = train.carriages.map { new Room(this, _) }

  /** Total travel distance. */
  private val distance: Double = (roads.map { _.length }).sum

  /** Position on the current route, as a distance. */
  private var currentRouteDistanceDone: Double = 0

  /** Remaining routes, including the current one. */
  private var remainingRoutes: List[Route] = roads

  private var state: State.Val = State.Launched

  /** The next town to reach. */
  def nextTown: Town = currentRoute.end
  /** Travel destination. */
  def destination: Town = roads.last.end
  /** Current travel route. */
  def currentRoute: Route = remainingRoutes.head
  /** Last town reached. */
  def currentTown: Town = train.town
  /** Destination reached. */
  def isDone: Boolean = remainingRoutes.isEmpty
  /** Tests if the travel will stop at a specific town.
   *
   *  Does not take past stops into account. */
  def stopsAt(t: Town): Boolean = (remainingRoutes.map { _.end}).contains(t)

  /** Distance remaining until destination. */
  def totalRemainingDistance: Double =
    (remainingRoutes.map { _.length }).sum - currentRouteDistanceDone
  /** Distance remaining until next stop. */
  def remainingDistance: Double = currentRoute.length - currentRouteDistanceDone

  /** Distance remaining until <code>destination</code>. 
    *
    * @param destination 
    * @throws
    */ 
  def remainingDistanceTo(destination: Town): Double = {
    // BAD throw exception is destination not in the path. */
    if(destination == currentTown) return 0
    var remaining: Double = currentRoute.length
    var tmp = remainingRoutes
    while(tmp.head.end != destination) {
      tmp = tmp.tail
      remaining += tmp.head.length      
    }
    remaining
  }
  
  /** Time remaining until destination, without stop time. */
  def totalRemainingTime: Double = totalRemainingDistance / train.engine.speed
  /** Time remaining until next stop. */
  def remainingTime: Double = remainingDistance / train.engine.speed

  /** Position on the current route, as a proportion. */
  def currentRouteProportion: Double = currentRouteDistanceDone / currentRoute.length

  /** Number of passengers in the train. */
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

  /** Updates travel state.
   *
   *  @param dt the time passed since last update step.
   */
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
