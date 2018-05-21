import scalafx.Includes._
import scalafx.application._

import gui.MainStage
import logic.game._
import logic.company._
import ai._
import player._

object MainJFXApp extends JFXApp {
  /** Initializes the game and return the main player. */
  def gameInit(): Unit = {
    val playerList = List(new Player(company("Company name")),
                        new BasicTrainAI(company("Train AI"), 1.7, 0.5),
                        new BasicTruckAI(company("Truck AI"), 2, 0.6),
                        new BasicPlaneAI(company("Plane AI"), 1.9, 0.6),
                        new BasicShipAI(company("Ship AI"), 1.8, 0.7),
                        new GeneticAI(company("Advanced AI"), 3, 0))

    Game.init(playerList)
    Game.mainPlayer = Some(playerList(0))
  }

  /** Creates a new company.
    *
    * @param name The name of the company.
    */
  def company(name: String): Company = {
    val company = new Company(name, Game.world.towns.head)
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
