package game
import player._
import world._

object Game {
  private var last: Double = System.currentTimeMillis()
  var players: List[Player] = List() //[new Hero(), new IA("Dumb")]
  var world: World = null

  def update(): Unit = {
    val a = System.currentTimeMillis()
    val dt = a - last
    last = a

    logic(dt)
    draw(dt)
  }

  def logic(dt: Double): Unit = {
    //Trains
    players.foreach {p: Player => p.update(dt)}

    //Update Cities
    world.update(dt)
  }

  def draw(dt: Double): Unit = {
    //world.draw(dt)
  }
}
