package ai

import logic.company._
import logic.train._
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
class BasicAI(
  override val company: Company, 
  val actionDelay: Double, 
  var lastAction: Double)
extends Player(company) with AI {
  
  def play(world: World, dt: Double): Unit = {
    lastAction += dt
    if(lastAction > actionDelay) {
      lastAction = 0
      
      if(company.money > 3000 && company.engines.size < 10)
        company.buyEngine("Basic")
      if(company.money > 2000 && company.carriages.size < 50)
        company.buyCarriage("Basic")
      if(!company.enginesAvailable.isEmpty) {
        val train = company.createTrainFromEngine(company.enginesAvailable.head)
        if(!company.carriagesAvailable.isEmpty) {
          company.addCarriageToTrain(train, company.carriagesAvailable.head)
            company.launchTravel(train, Random.shuffle(train.town.neighbours).head)
        }
      }
      val trains = company.trainsAvailable.filter { !_.carriages.isEmpty }
      if(!trains.isEmpty)
        company.launchTravel(trains.head, 
               Random.shuffle(Random.shuffle(trains).head.town.neighbours).head)
    }
  }
}
