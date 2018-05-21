import scalafx.Includes._
import scalafx.application._

import gui.MainStage
import logic.game._
import logic.company._
import logic.town._
import ai._
import player._

object MainJFXApp extends JFXApp {
  /** Initializes the game and return the main player. */
  def gameInit(): Unit = {
    Game.initWorld()
    val startTown: Town = Game.world.towns.head

    val player = new Player(company("Company name", startTown))
    val players: List[Player] = List(
      player,
      new BasicTrainAI(company("Train AI", startTown), 1.7, 0.5),
      new BasicTruckAI(company("Truck AI", startTown), 2, 0.6),
      new BasicPlaneAI(company("Plane AI", startTown), 1.9, 0.6),
      new BasicShipAI(company("Ship AI", startTown), 1.8, 0.7),
      new GeneticAI(company("Advanced AI", startTown), 3, 0))
    Game.initPlayers(players, player)
  }

  /** Creates a new company.
    *
    * @param name The name of the company.
    */
  def company(name: String, town: Town): Company = {
    val company = new Company(name, town)
    company.credit(20000)
    company
  }

  var mainStage = new MainStage(() => gameInit())
  stage = mainStage

  /** Allows [[gui.MainStage]] to perform cleanup. */
  override def stopApp(): Unit = {
    mainStage.onExit()
  }
}
