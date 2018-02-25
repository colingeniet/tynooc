package logic.travel

import logic.train._
import logic.world._
import logic.town._
import logic.room._

object State {
  sealed trait Val
  case object WAITING extends Val //Waiting for passengers
  case object ON_ROAD extends Val 
  case object ARRIVED extends Val //Passengers are leaving now  
}

class Travel(val train: Train, private val roads: List[World.Route], 
             val owner: Player, private val rooms : List[Rooms]) {
  
  if(!owner.owns_train(train)) 
    throw new IllegalArgumentException("Player doesn’t own the train")

  private val distance: Double = (roads.map { _.length }).sum
  private var distanceDone: Double = 0
  private var remainingRoads: List[World.Route] = roads
  private var actualRoad: World.Route = remaining_stops.head
  private val actualTown: World.Town = actual_road.start
  private var state: State.Val = State.WAITING
  /* private var actualRoadIndex = 0 */
  
  def next_step: World.Town = actual_road.end
  def destination: World.Town = roads.last.end
  def town: World.Town = actual_town
  def isDone: Boolean = remaining_roads.isEmpty
  
  def totalRemainingDistance: Double = (remainingRoads.map { _.length }).sum - distanceDone
  def remainingDistance: Double = actualRoad.length - distanceDone 
  def remainingTime: Double = remainingDistance / train.engine.speed 
  
  def passengerNumber: Int = (rooms.maps { _.passengerNumber}).sum 
  
  def isWaiting:  State.Val = state == WAITING
  def isOnRoad:   State.Val = state == ON_ROAD
  def is_arrived: State.Val = state == ARRIVED
  
  def availableRooms: List[Rooms] = rooms.filter { _.isAvailable }
  
  def landPassengers: Unit = {
    rooms.foreach { room => 
      Game.world.Status.each { status => 
        city.addResident(room[status.id], status)
        room.freePlaces(status, actualTown)
      }     
    }
  }
  
  def update(dt: Double): Unit = {
    return if(is_done)
    
    state match {
      case State.ON_ROAD => {
        distanceDone += World.realToVirtualTime(dt) * train.engine.speed
        if distanceDone >= actualRoad.distance {
          state = State.ARRIVED
          distanceDone = 0
        }
      }
      case State.ARRIVED => {
        train.deteriorate(actualRoad)
        state = State.WAITING
        remainingRoads = remainingRoads.tail
        /* actualRoadIndex += 1 */
        actualRoad = remainingRoads.head
      }
      case State.WAITING => {
        state = State.ON_ROAD
        actual_town = actualRoad.destination
      }
    }
  } 
}
