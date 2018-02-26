package logic.player

import logic.world._
import logic.train._
import logic.ticket._

class Travel(t: Train, l : List[World.Route], own: Player, tl : List[Ticket]) {

  /*
  * Represent the State of the Travel
  *
  */
  object State {
    sealed trait Val
    case object WAITING extends Val //Waiting for passengers
    case object ON_ROAD extends Val
    case object ARRIVED extends Val //Passengers are leaving now
  }

  if(! own.trains.contains(t))
    throw new IllegalArgumentException("The player doesn't own the train !")

  val owner: Player = own//redudant with train but keep it nonetheless
  val train: Train = t
  var stops: List[World.Route] = l
  private var distance_done: Double = 0
  private var current: Int = 0 //The road you're taking
  private var route: World.Route = null
  private val tickets: List[Ticket] = tl
  var state: State.Val = State.WAITING

  def distanceRemaining: Double = {

    var d = stops.foldLeft[Double](0) { (acc, r) => acc + r.length }
    return d - distance_done
  }

  def timeRemaining: Double = {
    distanceRemaining/t.engine.model.speed
  }

  def next: World.Town = {
    stops(current).destination
  }

  def is_done: Boolean = {
    stops.isEmpty
  }

  def is_waiting: Boolean = {
    state == State.WAITING
  }

  def is_arrived: Boolean = {
    state == State.ARRIVED
  }

  def town: World.Town = {
    route.start
  }

  def destination: World.Town = {
    route.destination
  }

  def update(dt: Double): Unit = {

    if(is_done)
      return

    state match {

      case State.ON_ROAD => {

        distance_done += World.real_to_virtual_time(dt)*train.engine.model.speed

        if(distance_done > route.length)
        {
          state = State.ARRIVED
          distance_done = 0
        }
      }

      case State.ARRIVED => {
        train.deteriorate(route)
        state = State.WAITING
        stops = stops.tail
        current += 1
        route = stops.head
      }

      case State.WAITING => {
        state = State.ON_ROAD
      }

    }
  }

  def available_tickets: List[Ticket] =
  {
    tickets.filter { _.available }
  }
}

object PriceSimulation {
  def upgradePrice(from: Engine, to: EngineModel): Double = {
    to.price-from.model.price
  }

  def upgradePrice(from: Carriage, to: CarriageModel): Double = {
    to.price-from.model.price
  }
}

class Player() {
  var trains: List[Train] = List()
  var carriages: List[Carriage] = List()
  var engines: List[Engine] = List()
  var travels: List[Travel] = List()

  var money: Double = 0

  def addMoney(m: Int): Unit = {
    money += m
  }

  def buyEngine(name: String): Unit = {
    var c = EngineModel(name)
    if (c.price <= money) {
      this.money -= c.price
      engines = new Engine(c) :: engines
    }
  }

  def buyCarriage(name: String): Unit = {
    var c = CarriageModel(name)
    if (c.price <= money) {
      this.money -= c.price
      carriages = new Carriage(c) :: carriages
    }
  }

  def assembleTrain(e: Engine, c: List[Carriage]): Unit = {
    engines = engines diff List(e)
    carriages = carriages diff c
    trains = new Train(e, c) :: trains
  }

  def launchTravel(train:Train, to:World.Town): Unit = {
    //(new Travel(train, World.findPath(train.where, to)))::travels
  }

  def editEngine(old: Engine, model: EngineModel): Unit = {
    if (money >= PriceSimulation.upgradePrice(old, model)) {
      old.model = model
      money -= PriceSimulation.upgradePrice(old, model)
    }
  }

  def editCarriage(old: Carriage, model: CarriageModel): Unit = {
    if (money >= PriceSimulation.upgradePrice(old, model)) {
      old.model = model
      money -= PriceSimulation.upgradePrice(old, model)
    }
  }

  def update(dt: Double): Unit = {
    travels.foreach{ t:Travel => t.update(dt)}
  }
}
