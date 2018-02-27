import scalafx.Includes._
import scalafx.application.JFXApp

import gui.MainStage
import logic.world._
import logic.game._
import logic.town._

object MainJFXApp extends JFXApp {
  Game.world = new World()
  var town1: Town = new Town("Cachan", 500, 300, 1)
  var town2: Town = new Town("Rennes", 50, 250, 0.6)
  var town3: Town = new Town("Ulm", 500, 200, 0.7)
  var town4: Town = new Town("Lyon", 700, 700, 0.85)

      town1.addRoute(town2, 150)
      town2.addRoute(town1, 150)
      town1.addRoute(town3, 30)
      town3.addRoute(town1, 30)
      town1.addRoute(town4, 250)
      town4 .addRoute(town1, 250)
  Game.world.addTown(town1)
  Game.world.addTown(town2)
  Game.world.addTown(town3)
  Game.world.addTown(town4)

  var mainstage = new MainStage
  mainstage.world = Game.world
  stage = mainstage
}
