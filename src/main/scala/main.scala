import scalafx.Includes._
import scalafx.application.JFXApp

import gui.MainStage
import logic.world.World

object MainJFXApp extends JFXApp {
  var world: World = new World
  var town1: World.Town = new World.Town("Tyn Town", 100, 200, 0.4)
  var town2: World.Town = new World.Town("Nooc Town", 300, 300, 0.6)
  town1.addRoute(town2, 100)
  world.addTown(town1)
  world.addTown(town2)

  var mainstage = new MainStage
  mainstage.world = world
  stage = mainstage
}
