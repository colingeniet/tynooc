import scalafx.Includes._
import scalafx.application._

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

  town1.addRoute(town2, 1500)
  town2.addRoute(town1, 1500)
  town1.addRoute(town3, 300)
  town3.addRoute(town1, 300)
  town1.addRoute(town4, 2500)
  town4 .addRoute(town1, 2500)
  Game.world.addTown(town2)
  Game.world.addTown(town3)
  Game.world.addTown(town4)
  Game.world.addTown(town1)

  var mainstage = new MainStage(Game.world)
  stage = mainstage

  var mainLoopThread = new Thread {
    override def run {
      while(true) {
        Game.update()
        Platform.runLater(mainstage.draw())
        Thread.sleep(33)
      }
    }
  }
  mainLoopThread.start

  override def stopApp(): Unit = {
    mainLoopThread.stop()
  }
}
