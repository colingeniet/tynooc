import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene

class MainStage extends JFXApp.PrimaryStage {
  var mainMenuScene: DisplayScene = new MainMenuScene(changeScene)
  var gameScene: DisplayScene = new GameScene(changeScene)

  title.value = "Tynooc"
  scene = mainMenuScene

  def changeScene(newScene: DisplayStates.Val): Unit = {
    newScene match {
      case DisplayStates.MainMenu => scene = mainMenuScene
      case DisplayStates.GameMenu => ()
      case DisplayStates.Game => scene = gameScene
      case DisplayStates.Options => ()
    }
  }
}
