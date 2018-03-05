import scalafx.Includes._
import scalafx.application._

import gui.MainStage
import logic.world._
import logic.game._
import logic.town._
import logic.company._
import parser._
import ia._

object MainJFXApp extends JFXApp {
  /** Initializes the game. */
  def gameInit(): Unit = {
    Game.reset()
    Game.world = Parser.readWorldInformations("Map")
    Game.ia = new BasicIA(player())
  }

  /** Creates a new company. */
  def company(): Company = {
    val company = new Company(Game.world.towns.find(_.name == "C411").get)
    company.credit(10000)
    company
  }

  var mainStage = new MainStage(() => gameInit(), () => company())
  stage = mainStage

  /** Allows [[MainStage]] to perform cleanup. */
  override def stopApp(): Unit = {
    mainStage.onExit()
  }
}
