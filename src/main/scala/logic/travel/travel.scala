package logic.travel

import scalafx.beans.property._
import scalafx.beans.binding._
import scalafx.collections._
import scalafx.beans.binding.BindingIncludes._

import logic.vehicle._
import logic.world._
import logic.town._
import logic.room._
import logic.route._
import logic.company._
import logic.game._

private object State {
  sealed trait Val
  case object Launched extends Val
  case object Waiting extends Val //Waiting for passengers
  case object OnRoute extends Val
  case object Arrived extends Val //Passengers are leaving now
}

/** A travel. */
class Travel(val vehicle: Vehicle, private val routes: List[Route]) {
  val company = vehicle.owner()

  private val rooms: List[Room] = vehicle.createRooms(this)

  /** Total travel distance. */
  private val distance: Double = (routes.map { _.length }).sum

  /** Position on the current route, as a distance. */
  private val currentRouteDistanceDone: DoubleProperty = DoubleProperty(0)

  /** Remaining routes, including the current one. */
  private val remainingRoutes: ObservableBuffer[Route] = ObservableBuffer(routes)

  private val state: ObjectProperty[State.Val] = ObjectProperty(State.Launched)

  /** Travel destination. */
  val destination: Town = routes.last.end

  var onCompleted: () => Unit = () => ()

  /** Current travel route. */
  val currentRoute: ObjectBinding[Option[Route]] =
    createObjectBinding(
      () => remainingRoutes.headOption,
      remainingRoutes)

  /** The next town to reach. */
  val nextTown: ObjectBinding[Town] =
    createObjectBinding(
      () => currentRoute() match {
        case Some(r) => r.end
        case None => destination
      },
      currentRoute)

  /** Last town reached. */
  val currentTown: ObjectProperty[Town] = vehicle.town

  /** Destination reached. */
  val isDone: BooleanBinding = createBooleanBinding(
    () => remainingRoutes.isEmpty,
    remainingRoutes)


  def remainingStops: List[Town] = {
    remainingRoutes.map(_.end).filter(_.accepts(vehicle)).toList
  }

  /** Tests if the travel will stop at a specific town.
   *
   *  Does not take past stops into account. */
  def stopsAt(t: Town): Boolean = {
    remainingStops.contains(t)
  }

  /** Distance remaining until destination. */
  /* bindings are calculated lazily */
  val totalRemainingDistance: NumberBinding =
    jfxNumberBinding2sfx(createDoubleBinding(
      () => remainingRoutes.toList.map(_.length).sum - currentRouteDistanceDone(),
      remainingRoutes,
      currentRouteDistanceDone))


  /** Distance remaining until next stop. */
  val remainingDistance: NumberBinding =
    jfxNumberBinding2sfx(createDoubleBinding(
      () => currentRoute() match {
        case Some(r) => r.length - currentRouteDistanceDone()
        case None => 0
      },
      currentRoute,
      currentRouteDistanceDone))

  /** Distance remaining until <code>destination</code>.
    *
    * @param destination
    */
  def remainingDistanceTo(destination: Town): Double = {
    // BAD throw exception is destination not in the path. */
    if(destination == currentTown()) return 0
    var remaining: Double = remainingDistance.toDouble
    var tmp = remainingRoutes.toList
    while(tmp.head.end != destination) {
      tmp = tmp.tail
      remaining += tmp.head.length
    }
    remaining
  }

  def remainingTimeTo(to: Town): Double = remainingDistanceTo(to) / vehicle.speed

  /** Time remaining until destination, without stop time. */
  val totalRemainingTime: NumberBinding = totalRemainingDistance / vehicle.speed
  /** Time remaining until next stop. */
  val remainingTime: NumberBinding = remainingDistance / vehicle.speed

  /** Position on the current route, as a proportion. */
  val currentRouteProportion: NumberBinding =
    jfxNumberBinding2sfx(createDoubleBinding(
      () => currentRoute() match {
        case Some(r) => currentRouteDistanceDone() / r.length
        case None => 0
      },
      currentRoute,
      currentRouteDistanceDone))

  val posX: NumberBinding =
    jfxNumberBinding2sfx(createDoubleBinding(
      () => currentRoute() match {
        case Some(r) => {
          val p: Double = currentRouteProportion.toDouble
          r.start.x * (1-p) + r.end.x * p
        }
        case None => 0
      },
      currentRoute,
      currentRouteProportion))

  val posY: NumberBinding =
    jfxNumberBinding2sfx(createDoubleBinding(
      () => currentRoute() match {
        case Some(r) => {
          val p: Double = currentRouteProportion.toDouble
          r.start.y * (1-p) + r.end.y * p
        }
        case None => 0
      },
      currentRoute,
      currentRouteProportion))

  val heading: NumberBinding =
    jfxNumberBinding2sfx(createDoubleBinding(
      () => currentRoute() match {
        case Some(r) => math.atan2(r.end.x - r.start.x, r.start.y - r.end.y).toDegrees
        case None => 50
      },
      currentRoute))


  /** Number of passengers in the vehicle. */
  val passengerNumber: IntegerProperty = IntegerProperty(0)

  val isWaiting: BooleanBinding = jfxBooleanBinding2sfx(state === State.Waiting)
  val isLaunched: BooleanBinding = jfxBooleanBinding2sfx(state === State.Launched)
  val isOnRoute: BooleanBinding = jfxBooleanBinding2sfx(state === State.OnRoute)
  val isArrived: BooleanBinding = jfxBooleanBinding2sfx(state === State.Arrived)

  def isWaitingAt(town: Town): Boolean = {
    (isWaiting() || isLaunched()) && currentTown() == town && town.accepts(vehicle)
  }

  def availableRooms: List[Room] = rooms.filter { _.isAvailable }

  def landPassengers(): Unit = {
    rooms.foreach { room =>
      currentTown().addResidents(room.passengers(currentTown()))
      room.freePlaces(currentTown())
    }
  }

  /** Updates travel state.
   *
   *  @param dt the time passed since last update step.
   */
  def update(dt: Double): Unit = {
    if(!isDone()) {
      state() match {
        case State.Launched => state() = State.Waiting
        case State.OnRoute => {
          currentRouteDistanceDone() += dt * vehicle.speed(currentRoute().get)
          if(currentRouteDistanceDone() >= currentRoute().get.length) {
            state() = State.Arrived
          }
        }
        case State.Arrived => {
          company.debit(vehicle.consumption(currentRoute().get.length) * Game.world.fuelPrice)
          state() = State.Waiting
          if(currentTown().accepts(vehicle)) {
            landPassengers()
          }
          remainingRoutes.remove(0)
          currentRouteDistanceDone() = 0
          if(isDone()) {
            vehicle.travel() = None
            onCompleted()
          }
        }
        case State.Waiting => {
          rooms.foreach(r => {
            r.embarkAll(currentTown())
            r.loadAll(currentTown())
          })
          vehicle.town() = nextTown()
          passengerNumber() = (rooms.map { _.passengerNumber}).sum
          state() = State.OnRoute
        }
      }
    }
  }
}
