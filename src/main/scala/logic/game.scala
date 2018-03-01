package logic.game

import logic.player._
import logic.world._

object Game {
  private var last: Double = System.currentTimeMillis()
  var players: List[Player] = List() //[new Hero(), new IA("Dumb")]
  var world: World = null
  var time: Double = 0

  var paused: Boolean = false
  var timeAcceleration: Double = 1

  def update(): Unit = {
    val a: Double = System.currentTimeMillis()
    if (!paused) {
      val dt: Double = timeAcceleration * realToVirtualTime((a - last) / 1000)
      logic(dt)
      time += dt
    }
    last = a
  }

  def logic(dt: Double): Unit = {
    //Trains
    players.foreach(_.update(dt))

    //Update Cities
    world.update(dt)
  }

  def realToVirtualTime(t: Double) : Double = t
  def virtualToRealTime(t: Double) : Double = t
}
