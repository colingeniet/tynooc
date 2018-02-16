import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.application.Platform

class MainStage extends JFXApp.PrimaryStage {
  private var mainMenuScene: DisplayScene = new MainMenuScene(changeScene)
  private var gameScene: DisplayScene = new GameScene(changeScene)
  private var optionsScene: DisplayScene = new OptionsScene(changeScene)

  title.value = "Tynooc"
  scene = mainMenuScene

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
