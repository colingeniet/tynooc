import scalafx.Includes._
import scalafx.application._

import gui.MainStage
import logic.game._
import logic.company._
import ai._
import player._

object MainJFXApp extends JFXApp {
  /** Initializes the game and return the main player. */
  def gameInit(): Player = {
    Game.init()
    Game.players = List(new Player(company("Company name")),
                        new BasicTrainAI(company("AI 1 company"), 1.7, 0.5),
                        new BasicTruckAI(company("AI 2 company"), 2, 0.6),
                        new BasicPlaneAI(company("AI 3 company"), 1.9, 0.6))
    Game.players.foreach { p => Game.world.addCompany(p.company) }
    Game.mainPlayer = Some(Game.players(0))
    Game.mainPlayer.get
  }

  /** Creates a new company.
    *
    * @param name The name of the company.
    */
  def company(name: String): Company = {
    val company = new Company(name, Game.world.towns.head)
    company.credit(1000000)
    company
  }

  var mainStage = new MainStage(() => gameInit())
  stage = mainStage

  /** Allows [[gui.MainStage]] to perform cleanup. */
  override def stopApp(): Unit = {
    mainStage.onExit()
  }
}
