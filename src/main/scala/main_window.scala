import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.application.Platform

/** Scenes available to display by the MainStage.
 */
object DisplayStates {
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
abstract class DisplayScene(sceneModifier: DisplayStates.Val=>Unit) extends Scene


/** Main window manager.
 *
 *  Handles, displays and switch between menus and game screens.
 */
class MainStage extends JFXApp.PrimaryStage {
  /* Actual scenes displayed. */
  private var mainMenuScene: DisplayScene = new MainMenuScene(changeScene)
  private var gameScene: DisplayScene = new GameScene(changeScene)
  private var optionsScene: DisplayScene = new OptionsScene(changeScene)

  /* Stage configuration. */
  title.value = "Tynooc"
  scene = mainMenuScene
  width = 1024
  height = 720

  /** Callback method that can be used to switch scene.
   *
   *  @param newScene the scene to switch to.
   */
  def changeScene(newScene: DisplayStates.Val): Unit = {
    newScene match {
      case DisplayStates.MainMenu => scene = mainMenuScene
      case DisplayStates.GameMenu => ()
      case DisplayStates.Game => scene = gameScene
      case DisplayStates.Options => scene = optionsScene
      case DisplayStates.Quit => Platform.exit ()
    }
  }
}
