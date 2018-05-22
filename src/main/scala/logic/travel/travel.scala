package logic.travel

import scala.collection.mutable.HashMap

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
import logic.good._
import logic.facility._
import utils._

import java.io._

private object State {
  sealed trait Val
  case object Launched extends Val
  case object Waiting extends Val //Waiting for passengers
  case object OnRoute extends Val
  case object Arrived extends Val //Passengers are leaving nows
}

/** A travel. */
@SerialVersionUID(0L)
class Travel(val vehicle: Vehicle, private val routes: List[Route])
extends Serializable {
  val company = vehicle.owner()

  private val rooms: List[Room] = vehicle.createRooms(this)

  /** Total travel distance. */
  private val distance: Double = (routes.map { _.length }).sum

  /** Position on the current route, as a distance. */
  @transient private var currentRouteDistanceDone: DoubleProperty = DoubleProperty(0)

  /** Remaining routes, including the current one. */
  @transient private var remainingRoutes: ObservableBuffer[Route] = ObservableBuffer(routes)

  @transient private var state: ObjectProperty[State.Val] = ObjectProperty(State.Launched)

  /** Travel destination. */
  val destination: Town = routes.last.end

  var onCompleted: () => Unit = () => ()

  /** Current travel route. */
  @transient var currentRoute: ObjectBinding[Option[Route]] = null
  /** The next town to reach. */
  @transient var nextTown: ObjectBinding[Town] = null

  /** Last town reached. */
  @transient var currentTown: ObjectProperty[Town] = null
  /** Destination reached. */
  @transient var isDone: BooleanBinding = null

  /** Distance remaining until destination. */
  /* bindings are calculated lazily */
  @transient var totalRemainingDistance: NumberBinding = null
  /** Distance remaining until next stop. */
  @transient var remainingDistance: NumberBinding = null

  /** Time remaining until destination, without stop time. */
  @transient var totalRemainingTime: NumberBinding = null
  /** Time remaining until next stop. */
  @transient var remainingTime: NumberBinding = null

  /** Position on the current route, as a proportion. */
  @transient var currentRouteProportion: NumberBinding = null
  @transient var posX: NumberBinding = null
  @transient var posY: NumberBinding = null
  @transient var heading: NumberBinding = null

  /** Number of passengers in the vehicle. */
  @transient var passengerNumber: IntegerProperty = IntegerProperty(0)
  @transient var contents: HashMap[Good, DoubleProperty] =
    InitHashMap[Good, DoubleProperty](_ => DoubleProperty(0))

  @transient var isWaiting: BooleanBinding = null
  @transient var isLaunched: BooleanBinding = null
  @transient var isOnRoute: BooleanBinding = null
  @transient var isArrived: BooleanBinding = null


  private var atTownCallbackMap: HashMap[Town, List[() => Unit]] =
    InitHashMap(_ => List())

  /** Execute [[callback]] upon reaching [[town]] */
  def atTownCallback(town: Town, callback: () => Unit): Unit = {
    atTownCallbackMap(town) = callback :: atTownCallbackMap(town)
  }


  private def initBindings(): Unit = {
    currentRoute = createObjectBinding(
        () => remainingRoutes.headOption,
        remainingRoutes)

    nextTown = createObjectBinding(
        () => currentRoute() match {
          case Some(r) => r.end
          case None => destination
        },
        currentRoute)

    currentTown = vehicle.town
    isDone = createBooleanBinding(
      () => remainingRoutes.isEmpty,
      remainingRoutes)

    totalRemainingDistance =
      jfxNumberBinding2sfx(createDoubleBinding(
        () => remainingRoutes.toList.map(_.length).sum - currentRouteDistanceDone(),
        remainingRoutes,
        currentRouteDistanceDone))

    remainingDistance =
      jfxNumberBinding2sfx(createDoubleBinding(
        () => currentRoute() match {
          case Some(r) => r.length - currentRouteDistanceDone()
          case None => 0
        },
        currentRoute,
        currentRouteDistanceDone))

    totalRemainingTime = totalRemainingDistance / vehicle.speed
    remainingTime = remainingDistance / vehicle.speed

    currentRouteProportion =
      jfxNumberBinding2sfx(createDoubleBinding(
        () => currentRoute() match {
          case Some(r) => currentRouteDistanceDone() / r.length
          case None => 0
        },
        currentRoute,
        currentRouteDistanceDone))

    posX = jfxNumberBinding2sfx(createDoubleBinding(
      () => currentRoute() match {
        case Some(r) => {
          val p: Double = currentRouteProportion.toDouble
          r.start.x * (1-p) + r.end.x * p
        }
        case None => 0
      },
      currentRoute,
      currentRouteProportion))
    posY = jfxNumberBinding2sfx(createDoubleBinding(
      () => currentRoute() match {
        case Some(r) => {
          val p: Double = currentRouteProportion.toDouble
          r.start.y * (1-p) + r.end.y * p
        }
        case None => 0
      },
      currentRoute,
      currentRouteProportion))
    heading = jfxNumberBinding2sfx(createDoubleBinding(
      () => currentRoute() match {
        case Some(r) => math.atan2(r.end.x - r.start.x, r.start.y - r.end.y).toDegrees
        case None => 0
      },
      currentRoute))

    isWaiting = jfxBooleanBinding2sfx(state === State.Waiting)
    isLaunched = jfxBooleanBinding2sfx(state === State.Launched)
    isOnRoute = jfxBooleanBinding2sfx(state === State.OnRoute)
    isArrived = jfxBooleanBinding2sfx(state === State.Arrived)
  }
  initBindings()


  def remainingStops: List[Town] = {
    remainingRoutes.map(_.end).filter(_.accepts(vehicle)).toList
  }

  /** Tests if the travel will stop at a specific town.
   *
   *  Does not take past stops into account. */
  def stopsAt(t: Town): Boolean = {
    remainingStops.contains(t)
  }

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

  def unload(): Unit = {
    rooms.foreach(_.unloadAll(currentTown()))
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
          assert(currentRoute != null)
          currentRouteDistanceDone() += dt * vehicle.speed(currentRoute().get)
          rooms.foreach(_.handleGoods(dt))
          if(currentRouteDistanceDone() >= currentRoute().get.length) {
            state() = State.Arrived
          }
        }
        case State.Arrived => {
          company.debit(vehicle.consumption(currentRoute().get.length) * Game.world.fuelPrice)
          state() = State.Waiting

          if(currentTown().accepts(vehicle)) {
            vehicle match {
              case v: Truck => ()
              case v: Tank => ()
              case _         => {
                val stations = currentTown().stationsFor(vehicle)
                val s = stations.find(_.owner() == vehicle.owner()).getOrElse(stations.head)
                s.onEnter(vehicle)
              }
            }
            landPassengers()
            unload()
          }

          atTownCallbackMap(currentTown()).foreach(_())
          atTownCallbackMap(currentTown()) = List()

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
          Good.all.foreach(g => { contents(g)() = rooms.map(_.goods(g)).sum })
          state() = State.OnRoute
        }
      }
    }
  }

  @throws(classOf[IOException])
  private def writeObject(stream: ObjectOutputStream): Unit = {
    stream.defaultWriteObject()
    stream.writeObject(this.currentRouteDistanceDone.toDouble)
    stream.writeObject(this.remainingRoutes.toList)
    stream.writeObject(this.state())
    stream.writeObject(this.passengerNumber.toInt)
    stream.writeObject(this.contents.map{ case(g,v) => (g,v.toDouble) })
  }

  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  private def readObject(stream: ObjectInputStream): Unit = {
    stream.defaultReadObject()
    this.currentRouteDistanceDone = DoubleProperty(stream.readObject().asInstanceOf[Double])
    this.remainingRoutes = ObservableBuffer[Route](stream.readObject().asInstanceOf[List[Route]])
    this.state = ObjectProperty(stream.readObject().asInstanceOf[State.Val])
    this.passengerNumber = IntegerProperty(stream.readObject().asInstanceOf[Integer])

    this.contents = InitHashMap[Good, DoubleProperty](_ => DoubleProperty(0))
    val new_contents = stream.readObject().asInstanceOf[HashMap[Good,Double]]
    new_contents.foreach{ case (g,v) => this.contents(g)() = v }

    this.initBindings()
  }
}
