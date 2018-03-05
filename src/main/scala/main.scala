import scalafx.Includes._
import scalafx.application._

import gui.MainStage
import logic.world._
import logic.game._
import logic.town._
import logic.company._
import parser._
import ai._
import player._

object MainJFXApp extends JFXApp {
  /** Initializes the game. */
  def gameInit(): Player = {
    Game.reset()
    Game.world = Parser.readWorldInformations("map/Map")
    Game.players = List(new Player(company()),
                        new BasicAI(company(), 1.7, 0.5))
    Game.mainPlayer = Some(Game.players(0))
    Game.mainPlayer.get
  }

  /** Creates a new company. */
  def company(): Company = {
    val company = new Company(Game.world.towns.find(_.name == "C411").get)
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
