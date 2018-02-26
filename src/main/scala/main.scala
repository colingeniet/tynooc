import logic.world._
import logic.game._
import logic.town._

object Program {
  def main(args:Array[String]):Unit = {
    Game.world = new World()
    var town1: Town = new Town("Cachan", 100, 100, 1)
    var town2: Town = new Town("Rennes", 0, 120, 0.6)
    var town3: Town = new Town("Ulm", 10, 105, 0.7)
    var town4: Town = new Town("Lyon", 110, 0, 0.85)

    town1.addRoute(town2, 100)
    town2.addRoute(town1, 100)
    town3.addRoute(town2, 150)
    town2.addRoute(town3, 150)
    town1.addRoute(town2, 100)
    town2.addRoute(town1, 100)
    town3.addRoute(town2, 150)
    town2.addRoute(town3, 150)
    Game.world.addTown(town1)
    Game.world.addTown(town2)
    Game.world.addTown(town3)
    Game.world.addTown(town4)
    println(Game.world)
  }
}
