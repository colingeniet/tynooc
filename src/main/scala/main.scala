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
                        new BasicAI(company("AI 1 company"), 1.7, 0.5),
                        new BasicAI(company("AI 2 company"), 2, 0.6))
    Game.players.foreach { p => Game.world.addCompany(p.company) }
    Game.mainPlayer = Some(Game.players(0))
    Game.mainPlayer.get
  }

  /** Creates a new company.
    *
    * @param name The name of the company.
    */
  def company(name: String): Company = {
    val company = new Company(name, Game.world.towns.find(_.name == "C411").get)
    company.credit(10000)
    company
  }

  var mainStage = new MainStage(() => gameInit())
  stage = mainStage

  /** Allows [[gui.MainStage]] to perform cleanup. */
  override def stopApp(): Unit = {
    mainStage.onExit()
  }
}
