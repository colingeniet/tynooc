import scalafx.Includes._
import scalafx.application._

import gui.MainStage
import logic.world._
import logic.game._
import logic.town._
import logic.player._
import parser._
import ia._

object MainJFXApp extends JFXApp {
  /** Initializes the game. */
  def gameInit(): Unit = {
    Game.reset()
    Game.world = Parser.readWorldInformations("Map")
    Game.ia = new BasicIA(player())
  }

  /** Creates a new player. */
  def player(): Player = {
    val player = new Player(Game.world.towns.find(_.name == "C411").get)
    player.credit(10000)
    player
  }

  var mainStage = new MainStage(() => gameInit(), () => player(), // None)
                      Some((p: Player) => new BasicIA(p)))
  stage = mainStage

  /** Allows [[MainStage]] to perform cleanup. */
  override def stopApp(): Unit = {
    mainStage.onExit()
  }
}
