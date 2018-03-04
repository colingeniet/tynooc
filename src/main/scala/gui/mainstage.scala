package gui

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.application.Platform

import gui.draw.Drawable
import gui.scenes._
import logic.world._
import logic.game._
import logic.player._
import ia._

/** Main window manager.
 *
 *  Handles, displays and switch between menus and game screens.
 */
class MainStage(gameInit: () => Unit, player: () => Player, 
                iaf: Option[Player => IA])
extends JFXApp.PrimaryStage with Drawable {
  gameInit()
  /* Actual scenes displayed. */
  private var playerV: Player = null
  private var ia: Option[IA] = None
  private var mainMenuScene: MainMenu = new MainMenu(changeScene)
  private var gameScene: Game = new Game(Game.world, player(), changeScene)
  private var optionsScene: Options = new Options(changeScene)

  private var onNextChangeCallback: () => Unit = () => ()

  /* Stage configuration. */
  title.value = "Tynooc"
  scene = mainMenuScene
  width = 1024
  height = 720

  /** Callback method that can be used to switch scene.
   *
   *  @param newScene the scene to switch to.
   */
  def changeScene(newScene: MainStage.States.Val): Unit = {
    onNextChangeCallback()
    onNextChangeCallback = () => ()

    newScene match {
      case MainStage.States.MainMenu => scene = mainMenuScene
      case MainStage.States.Game => {
        gameInit()
        playerV = player()
        
        if(iaf.isDefined)
          ia = Some(iaf.get(playerV))
        gameScene = new Game(Game.world, playerV, changeScene)
        scene = gameScene

        val mainLoopThread: Thread = new Thread {
          override def run {
            while(true) {
              Game.update(ia)
              Platform.runLater(draw())
              Thread.sleep(10)
            }
          }
        }
        mainLoopThread.start()
        onNextChangeCallback = () => mainLoopThread.stop()
      }
      case MainStage.States.Options => scene = optionsScene
      case MainStage.States.Quit => Platform.exit()
    }
  }

  override def draw(): Unit = {
    gameScene.draw()
  }

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
