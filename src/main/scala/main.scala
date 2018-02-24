import scalafx.Includes._
import scalafx.application.JFXApp

import gui.MainStage
import logic.world.World

object MainJFXApp extends JFXApp {
  var world: World = new World
  var town1: World.Town = new World.Town("Tyn", 100, 200, 0.4)
  var town2: World.Town = new World.Town("Nooc", 1500, 300, 0.6)
  var town3: World.Town = new World.Town("Test", 300, 1500, 0.5)
  town1.addRoute(town2, 100)
  town2.addRoute(town1, 100)
  town3.addRoute(town2, 150)
  town2.addRoute(town3, 150)
  world.addTown(town1)
  world.addTown(town2)
  world.addTown(town3)

  var mainstage = new MainStage
  mainstage.world = world
  stage = mainstage
}
