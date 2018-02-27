package gui

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.application.Platform

import gui.draw.Drawable
import gui.scenes._
import logic.world.World

/** Main window manager.
 *
 *  Handles, displays and switch between menus and game screens.
 */
class MainStage extends JFXApp.PrimaryStage with Drawable {
  /* Actual scenes displayed. */
  private var mainMenuScene: MainMenu = new MainMenu(changeScene)
  private var gameScene: Game = new Game(changeScene)
  private var optionsScene: Options = new Options(changeScene)
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
    newScene match {
      case MainStage.States.MainMenu => scene = mainMenuScene
      case MainStage.States.GameMenu => ()
      case MainStage.States.Game => scene = gameScene
      case MainStage.States.Options => scene = optionsScene
      case MainStage.States.Quit => Platform.exit ()
    }
  }

  def world: World = gameScene.world

  def world_=(newWorld: World): Unit = {
    gameScene.world = newWorld
  }

  override def draw(): Unit = {
    gameScene.draw()
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
    case object GameMenu extends Val
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
