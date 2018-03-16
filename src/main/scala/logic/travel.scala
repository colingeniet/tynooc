package logic.travel

import scalafx.beans.property._
import scalafx.beans.binding._
import scalafx.collections._
import scalafx.beans.binding.BindingIncludes._

import logic.train._
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
class Travel(val train: Train, private val routes: List[Route],
             val company: Company) {

  if(train.owner != company)
    throw new IllegalArgumentException("Company doesn’t own the train")

  private val rooms: List[Room] = train.carriages.toList.map { new Room(this, _) }

  /** Total travel distance. */
  private val distance: Double = (routes.map { _.length }).sum

  /** Position on the current route, as a distance. */
  private val currentRouteDistanceDone: DoubleProperty = DoubleProperty(0)

  /** Remaining routes, including the current one. */
  private val remainingRoutes: ObservableBuffer[Route] = ObservableBuffer(routes)

  private val state: ObjectProperty[State.Val] = ObjectProperty(State.Launched)

  /** Travel destination. */
  val destination: Town = routes.last.end

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
  val currentTown: ObjectProperty[Town] = train.town

  /** Destination reached. */
  val isDone: BooleanBinding = createBooleanBinding(
    () => remainingRoutes.isEmpty,
    remainingRoutes)

  /** Tests if the travel will stop at a specific town.
   *
   *  Does not take past stops into account. */
  def stopsAt(t: Town): Boolean = (remainingRoutes.map { _.end}).contains(t)

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

  /** Time remaining until destination, without stop time. */
  val totalRemainingTime: NumberBinding = totalRemainingDistance / train.engine.speed
  /** Time remaining until next stop. */
  val remainingTime: NumberBinding = remainingDistance / train.engine.speed

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


  /** Number of passengers in the train. */
  def passengerNumber: Int = (rooms.map { _.passengerNumber}).sum

  val isWaiting: BooleanBinding = jfxBooleanBinding2sfx(state === State.Waiting)
  val isLaunched: BooleanBinding = jfxBooleanBinding2sfx(state === State.Launched)
  val isOnRoute: BooleanBinding = jfxBooleanBinding2sfx(state === State.OnRoute)
  val isArrived: BooleanBinding = jfxBooleanBinding2sfx(state === State.Arrived)
  def isWaitingAt(town: Town): Boolean = (isWaiting() || isLaunched()) && currentTown() == town

  def availableRooms: List[Room] = rooms.filter { _.isAvailable }

  def landPassengers(): Unit = {
    rooms.foreach { room =>
      Game.world.status.foreach { status =>
        currentTown().addResidents(room.passengerNumber(status, currentTown()), status)
        room.freePlaces(currentTown(), status)
      }
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
          currentRouteDistanceDone() += dt * train.engine.speed
          if(currentRouteDistanceDone() >= currentRoute().get.length) {
            state() = State.Arrived
          }
        }
        case State.Arrived => {
          company.debit(train.consumption(currentRoute().get.length) * Game.world.fuelPrice)
          state() = State.Waiting
          landPassengers()
          remainingRoutes.remove(0)
          currentRouteDistanceDone() = 0
          if(isDone()) train.travel() = None
        }
        case State.Waiting => {
          train.town() = nextTown()
          state() = State.OnRoute
        }
      }
    }
  }
}
