package logic.game

import logic.player._
import logic.world._

object Game {
  private var last: Double = System.currentTimeMillis()
  var players: List[Player] = List() //[new Hero(), new IA("Dumb")]
  var world: World = null

  def update(): Unit = {
    val a: Double = System.currentTimeMillis()
    val dt: Double = (a - last) / 1000
    last = a

    logic(dt)
  }

  def logic(dt: Double): Unit = {
    //Trains
    players.foreach {p: Player => p.update(dt)}

    //Update Cities
    world.update(dt)
  }
}
