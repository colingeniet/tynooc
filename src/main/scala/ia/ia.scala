package ia

import logic.player._
import logic.train._
import logic.world._
import scala.util.Random

trait IA {
  val player: Player
  var last_action: Double
  val action_delay: Double
  
  def play(dt: Double)
}

class BasicIA(val player: Player)
extends IA {
  val action_delay = 2
  var last_action = 1.5
  
  def play(dt: Double): Unit = {
    last_action += dt
    if(last_action > action_delay) {
      last_action = 0
      if(player.money > 3000)
        player.buyEngine("Basic")
      if(player.money > 2000)
        player.buyCarriage("Basic")
      if(!player.enginesAvailable.isEmpty) {
        val train = player.createTrainFromEngine(player.enginesAvailable.head)
        if(!player.carriagesAvailable.isEmpty) {
          player.addCarriageToTrain(train, player.carriagesAvailable.head)
            player.launchTravel(train, Random.shuffle(train.town.neighbours).head)
        }
      }
      val trains = player.trainsAvailable.filter { !_.carriages.isEmpty }
      if(!trains.isEmpty)
        player.launchTravel(trains.head, 
               Random.shuffle(Random.shuffle(trains).head.town.neighbours).head)
    }
  }
}
