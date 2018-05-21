package ai

import logic.company._
import logic.vehicle._
import logic.world._
import scala.util.Random
import player._

import logic.travel._
import logic.town._
import logic.route._
import logic.good._
import logic.mission._

/** A trait representing an AI. */
trait AI {
  val company: Company
  var lastAction: Double
  val actionDelay: Double

  /** Make some actions for <code>company</code>.
    *
    * @param world The world where <code>company</code> progress.
    * @param dt The time passed since the last call of <code>play</code>.
    */
  def play(world: World, dt: Double)
}

/** A basic AI which plays randomly.
  *
  * @constructor Creates a basic AI with company, an action delay and the time of its last action.
  * @param company The company of this AI.
  * @param actionDelay The delay between the actions of the AI.
  * @param lastAction The time of the last action (it will play at <code>actionDelay</code> - <code>lastAction</code>).
  */
class BasicTrainAI(
  company: Company,
  val actionDelay: Double,
  var lastAction: Double)
extends Player(company) with AI {

  def play(world: World, dt: Double): Unit = {
    lastAction += dt
    if(lastAction > actionDelay) {
      lastAction = 0

      if(company.money() > 3000 && company.engines.size < 20)
        company.buy(Engine("basic", company))
      if(company.money() > 2000 && company.carriages.size < 100)
        company.buy(Carriage("basic", company))
      val engines = company.enginesAvailable
      if(!engines.isEmpty) {
        val train = engines.head
        val carriages = company.carriagesStoredAt(train.town()).filter { c =>
          train.weight.toDouble + c.model.weight < train.model.power
        }
        if(!carriages.isEmpty)
          company.addCarriageToTrain(train, carriages.head)
      }
      val trains = company.trainsAvailable.filter { !_.isEmpty() }
      if(!trains.isEmpty) {
        val train = Random.shuffle(trains).head
        val possibleDirections =
          world.townsAccessibleFrom(train.town(), train) diff List(train.town())

        if(!possibleDirections.isEmpty)
          company.launchTravel(train, Random.shuffle(possibleDirections).head)
      }
    }
  }
}

/* TODO : create a BasicAI[VehicleModel] class. */

class BasicPlaneAI(
  company: Company,
  val actionDelay: Double,
  var lastAction: Double)
extends Player(company) with AI {

  def play(world: World, dt: Double): Unit = {
    lastAction += dt
    if(lastAction > actionDelay) {
      lastAction = 0

      if(company.money() > 1000 && company.vehicles.size < 20)
        company.buy(Plane("basic", company))
      val planes = company.vehicles.toList.filter {!_.isUsed()}
      if(!planes.isEmpty) {
        val plane = Random.shuffle(planes).head
        val towns = world.townsAccessibleFrom(plane.town(), plane).filter(_ != plane.town())
        if (!towns.isEmpty) {
          val direction = Random.shuffle(towns).head
          company.launchTravel(plane, direction)
        }
      }
    }
  }
}

class BasicTruckAI(
  company: Company,
  val actionDelay: Double,
  var lastAction: Double)
extends Player(company) with AI {

  def play(world: World, dt: Double): Unit = {
    lastAction += dt
    if(lastAction > actionDelay) {
      lastAction = 0

      if(company.money() > 1000 && company.vehicles.size < 20)
        company.buy(Truck("basic", company))
      val trucks = company.vehicles.toList.filter {!_.isUsed()}
      if(!trucks.isEmpty) {
        val truck = Random.shuffle(trucks).head
        val towns = world.townsAccessibleFrom(truck.town(), truck).filter(_ != truck.town())
        if (!towns.isEmpty) {
          val direction = Random.shuffle(towns).head
          company.launchTravel(truck, direction)
        }
      }
    }
  }
}

class BasicShipAI(
  company: Company,
  val actionDelay: Double,
  var lastAction: Double)
extends Player(company) with AI {

  def play(world: World, dt: Double): Unit = {
    lastAction += dt
    if(lastAction > actionDelay) {
      lastAction = 0

      if(company.money() > 1000 && company.vehicles.size < 20)
        company.buy(Ship("basic", company))
      val ships = company.vehicles.toList.filter {!_.isUsed()}
      if(!ships.isEmpty) {
        val ship = Random.shuffle(ships).head
        val towns = world.townsAccessibleFrom(ship.town(), ship).filter(_ != ship.town())
        if (!towns.isEmpty) {
          val direction = Random.shuffle(towns).head
          company.launchTravel(ship, direction)
        }
      }
    }
  }
}

class BigBrotherAI(
  company: Company,
  val actionDelay: Double,
  var lastAction: Double)
extends Player(company) with AI {

  def play(world: World, dt: Double): Unit = {
    lastAction += dt
    if(lastAction > actionDelay) {
      lastAction = 0

      val max = 10
      var i = company.missions.length

      while(!company.waitingMissions.isEmpty && i < 10) {
        val m = company.waitingMissions.head
        m match {
          case (m : HelpMission) =>
            company.acceptMission(m)
            i = i+1
            val v = new Tank(TankModel.specialTankModel(m.good, m.quantity), m.from, company)
            company.buy(v)
            company.launchTravel(v, m.to)
          case ( m : FretMission) =>
        }
      }
    }
  }
}

class GeneticAI(
  company: Company,
  val actionDelay: Double,
  var lastAction: Double)
extends Player(company) with AI {

  type Data = (logic.vehicle.Vehicle, List[logic.town.Town])

  val N = 7 // Size of a path
  val M = 50 // Number of path generated
  val K = 15 // We intertwin optimal path with these random path
  val Q = 4 // The number of cities that will be taken from the random_path

  def comparison_function(a : (Data, Integer), b : (Data, Integer)) = {
    a._2 > b._2
  }

  /**
  * This function generates a random list of accessibles cities as opposed to the other one.
  * The other one generates a list of connected cities, thus making the number of cities traversed exactly n.
  **/
  def gen_path_1(world: World, n: Integer, v:Vehicle) : List[Town]= {
    val w = world.townsAccessibleFrom(v.town(), v).toArray

    if(w.length <= 1)
      return List()

    else {
      var prec = v.town()
      var p: List[Town] = List(v.town())

      for(i <- 1 to n) {
        var c = w(Random.nextInt(w.length))
        while(prec == c) {
          c = w(Random.nextInt(w.length))
        }
        p = c::p
      }
      return p.reverse
    }
  }

  def routes_from_towns(path : Data, world: World): List[Route] = {
    return path._2.tail.foldLeft((path._2.head, List() : List[Route])){case ((f: Town, l: List[Route]), t: Town) =>
      world.findPath(f, t, path._1) match {
        case None => throw new IllegalArgumentException("AI error: path not found")
        case Some (e : List[Route]) => (t, e reverse_::: l)
      }
    }._2.reverse
  }

  def estimated_cost_1(path:  Data) : Integer = {
    return 0
  }

  def fuse(opt : List[(Vehicle, List[Town])], rnd : List[(Vehicle, List[Town])]) :  List[(Vehicle, List[Town])] = {
    opt.zip(rnd).map { case (p1 : Data, p2 : Data) =>
      val inds = Random.shuffle((1 to p1._2.length)).takeRight(Q)
      var i = -1

      (p1._1, p1._2.zip(p2._2).map{ case (a : Town, b : Town) =>
        i = i + 1
        if(inds.contains(i)) //This could be a bit optimized if we decide to take a huge Q
          b
        else
          a
      })
    }
  }

  def buy_vehicles(): Unit = {
    if(company.money() > 5000 && company.vehicles.size < 20)
      company.buy(Truck("basic", company))
  }

  def play(world: World, dt: Double): Unit = {
    lastAction += dt
    if(lastAction > actionDelay) {
      lastAction = 0

      buy_vehicles()

      val vehicles = company.vehicles.toList.filter {!_.isUsed()}

      var path_list : List[(Vehicle, List[Town])] = List()

      for(i <- 1 to M) {
        val v = Random.shuffle(vehicles).head //Might prove to be inneficient, see if we can optimize
        path_list = (v, gen_path_1(world, N, v))::path_list
      }

      var wp = path_list.map{t => (t, estimated_cost_1(t)) }
      wp = wp.sortWith(comparison_function).reverse

      wp = wp.takeRight(K)
      var wp2 = wp.map{case (a : Data, b : Integer) => a}
      wp2 = fuse(wp2, wp2.map{t : Data => (t._1, gen_path_1(world, N, t._1)) })

      var wp3 = wp2.map{t => (t, estimated_cost_1(t))}
      wp3 = wp3.sortWith(comparison_function)

      val wp4 = wp3.map{case (a : Data, b : Integer) => a}
      val path = wp4.head //The optimal one

      val route = routes_from_towns(path, world)

      world.addTravel(new Travel(path._1, route))
    }
  }
}
