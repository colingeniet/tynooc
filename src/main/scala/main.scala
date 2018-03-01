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
  var town5: Town = new Town("X", 450, 350, 0)

  town1.addRoute(town2, 1500)
  town2.addRoute(town1, 1500)
  town1.addRoute(town3, 300)
  town3.addRoute(town1, 300)
  town1.addRoute(town4, 2500)
  town4 .addRoute(town1, 2500)
  Game.world.addTown(town2)
  Game.world.addTown(town3)
  Game.world.addTown(town4)
  Game.world.addTown(town5)
  Game.world.addTown(town1)

  town1.addResidents(1000, Status.WELL)
  town1.addResidents(400, Status.POOR)
  town1.addResidents(150, Status.RICH)
  town2.addResidents(1000, Status.WELL)
  town2.addResidents(300, Status.POOR)
  town2.addResidents(200, Status.RICH)
  town3.addResidents(1000, Status.WELL)
  town3.addResidents(100, Status.POOR)
  town3.addResidents(400, Status.RICH)
  town4.addResidents(1000, Status.WELL)
  town4.addResidents(250, Status.POOR)
  town4.addResidents(300, Status.RICH)

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
