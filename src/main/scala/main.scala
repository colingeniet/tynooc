import scalafx.Includes._
import scalafx.application._

import gui.MainStage
import logic.world._
import logic.game._
import logic.town._
import logic.player._

object MainJFXApp extends JFXApp {
  /** Initializes the game. */
  def gameInit(): Unit = {
    Game.reset()

    val town1: Town = new Town("Cachan", 500, 300, 1)
    val town2: Town = new Town("Rennes", 50, 250, 0.6)
    val town3: Town = new Town("Ulm", 500, 200, 0.7)
    val town4: Town = new Town("Lyon", 700, 700, 0.85)
    val town5: Town = new Town("X", 450, 350, 0)

    town1.addRoute(town2, 300, 1)
    town2.addRoute(town1, 300, 1)
    town1.addRoute(town3, 50, 1)
    town3.addRoute(town1, 50, 1)
    town1.addRoute(town4, 400, 1)
    town4 .addRoute(town1, 400, 1)
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
  }

  /** Creates a new player. */
  def player(): Player = {
    val player = new Player(Game.world.towns.find(_.name == "Cachan").get)
    player.addMoney(10000)
    player
  }

  var mainStage = new MainStage(() => gameInit(), () => player())
  stage = mainStage

  /** Allows [[MainStage]] to perform cleanup. */
  override def stopApp(): Unit = {
    mainStage.onExit()
  }
}
