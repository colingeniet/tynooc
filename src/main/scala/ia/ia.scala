package ia

import logic.company._
import logic.train._
import logic.world._
import scala.util.Random

trait IA {
  val company: Company
  var last_action: Double
  val action_delay: Double

  def play(dt: Double)
}

class BasicIA(val company: Company)
extends IA {
  val action_delay = 0.8
  var last_action = 0.6

  def play(dt: Double): Unit = {
    last_action += dt
    if(last_action > action_delay) {
      last_action = 0
      if(company.money > 3000)
        company.buyEngine("Basic")
      if(company.money > 2000)
        company.buyCarriage("Basic")
      if(!company.enginesAvailable.isEmpty) {
        val train = company.createTrainFromEngine(company.enginesAvailable.head)
        if(!company.carriagesAvailable.isEmpty) {
          company.addCarriageToTrain(train, company.carriagesAvailable.head)
            company.launchTravel(train, Random.shuffle(train.town.neighbours).head)
        }
      }
      val trains = company.trains.filter { t => !t.onRoute && !t.carriages.isEmpty }
      if(!trains.isEmpty)
        company.launchTravel(trains.head,
               Random.shuffle(Random.shuffle(trains).head.town.neighbours).head)
    }
  }
}
