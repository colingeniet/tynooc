package ai

import logic.company._
import logic.vehicle._
import logic.world._
import scala.util.Random
import player._

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

      if(company.money() > 3000)
        company.buy(Engine("basic", company))
      if(company.money() > 2000)
        company.buy(Carriage("basic", company))
      val engines = company.enginesAvailable
      if(!engines.isEmpty) {
        val train = engines.head
        val carriages = company.carriagesStoredAt(train.town()).filter { c =>
          train.weight() + c.model.weight < train.model.power
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

class BasicPlaneAI(
  company: Company,
  val actionDelay: Double,
  var lastAction: Double)
extends Player(company) with AI {

  def play(world: World, dt: Double): Unit = {
    lastAction += dt
    if(lastAction > actionDelay) {
      lastAction = 0

      if(company.money() > 1000)
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
