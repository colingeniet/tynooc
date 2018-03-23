package gui

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.application.Platform

import gui.scenes._
import logic.world._
import logic.game._
import ai._
import player._

/** Main window manager.
 *
 *  Handles, displays and switch between menus and game screens.
 */
class MainStage(gameInit: () => Player)
extends JFXApp.PrimaryStage {
  /* Actual scenes displayed. */
  private var mainMenuScene: MainMenu = new MainMenu(changeScene)
  private var gameScene: Game = null
  private var optionsScene: Options = new Options(changeScene)

  private var onNextChangeCallback: () => Unit = () => ()

  /* Stage configuration. */
  title.value = "Tynooc"
  scene = mainMenuScene
  width = 1024
  height = 720

  /** Changes the scene displayed.
   *
   *  @param newScene the scene to switch to.
   */
  def changeScene(newScene: MainStage.States.Val): Unit = {
    // call then reset callback
    onNextChangeCallback()
    onNextChangeCallback = () => ()

    // switch scene
    newScene match {
      case MainStage.States.MainMenu => scene = mainMenuScene
      case MainStage.States.Game => {
        var mainPlayer: Player = gameInit()
        gameScene = new Game(Game.world, mainPlayer, changeScene)
        scene = gameScene

        // launch background game loop when the game scene is selected
        val mainLoopThread: Thread = new Thread {
          override def run {
            while(true) {
              Platform.runLater(() => {
                Game.update()
              })
              Thread.sleep(33)
            }
          }
        }
        mainLoopThread.start()

        // kill background thread when leaving
        onNextChangeCallback = () => mainLoopThread.stop()
      }
      case MainStage.States.Options => scene = optionsScene
      case MainStage.States.Quit => Platform.exit()
    }
  }

  // run callback, event if exiting without `changeScene`
  def onExit(): Unit = {
    onNextChangeCallback()
  }
}

/** Main window manager companion object.
 */
object MainStage {
  /** Scenes available to display by the MainStage.
   */
  object States {
    sealed trait Val
    case object MainMenu extends Val
    case object Game     extends Val
    case object Options  extends Val
    case object Quit     extends Val
  }

  /** The type of a scene manipulated by the MainStage.
   *
   *  @param sceneModifier a callback that can be used to switch to
   *    a different scene.
   */
  abstract class Scene(sceneModifier: States.Val=>Unit)
  extends scalafx.scene.Scene
}
