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
  case object OnRoad extends Val
  case object Arrived extends Val //Passengers are leaving now
}

class Travel(val train: Train, private val roads: List[Route],
             val owner: Player, private val rooms : List[Room]) {

  if(!owner.owns(train))
    throw new IllegalArgumentException("Player doesn’t own the train")

  private val distance: Double = (roads.map { _.length }).sum
  private var distanceDone: Double = 0
  private var remainingRoads: List[Route] = roads
  private var actualRoad: Route = remainingRoads.head
  private var actualTown: Town = actualRoad.start
  private var state: State.Val = State.Waiting
  /* private var actualRoadIndex = 0 */

  def next_step: Town = actualRoad.end
  def destination: Town = roads.last.end
  def town: Town = actualTown
  def isDone: Boolean = remainingRoads.isEmpty

  def totalRemainingDistance: Double = (remainingRoads.map { _.length }).sum - distanceDone
  def remainingDistance: Double = actualRoad.length - distanceDone
  def remainingTime: Double = remainingDistance / train.engine.speed

  def passengerNumber: Int = (rooms.map { _.passengerNumber}).sum

  def isWaiting: Boolean = state == State.Waiting
  def isOnRoad: Boolean = state == State.OnRoad
  def isArrived: Boolean = state == State.Arrived

  def availableRooms: List[Room] = rooms.filter { _.isAvailable }

  def landPassengers: Unit = {
    rooms.foreach { room =>
      Game.world.status.foreach { status =>
        actualTown.addResidents(room.passengers(status.id)(actualTown.id), status)
        room.freePlaces(room.passengers(status.id)(actualTown.id), actualTown, status)
      }
    }
  }

  def update(dt: Double): Unit = {
    if(isDone)
      return
    state match {
      case State.OnRoad => {
        distanceDone = distanceDone + World.realToVirtualTime(dt) * train.engine.speed
        if(distanceDone >= actualRoad.length) {
          state = State.Arrived
          distanceDone = 0
        }
      }
      case State.Arrived => {
        train.deteriorate(actualRoad)
        state = State.Waiting
        remainingRoads = remainingRoads.tail
        /* actualRoadIndex += 1 */
        actualRoad = remainingRoads.head
      }
      case State.Waiting => {
        state = State.OnRoad
        actualTown = actualRoad.destination
      }
    }
  }
}
