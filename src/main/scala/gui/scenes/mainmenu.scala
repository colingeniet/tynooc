package gui.scenes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._
import scalafx.geometry._
import scalafx.stage._
import javafx.stage.FileChooser.ExtensionFilter

import gui.MainStage

import logic.game._

/** Game main menu.
 */
class MainMenu(sceneModifier: MainStage.States.Val => Unit, window: Window)
extends MainStage.Scene(sceneModifier) {
  private var gameBtn = sceneSwitchButton("Play", MainStage.States.Game)
  private var mapBtn = new Button("Select Map") {
    onAction = (event: ActionEvent) => {
      val fileChooser = new FileChooser {
        title = "open map file"
        initialDirectory = Game.mapPath.getParentFile()
        extensionFilters ++= Seq(
          new ExtensionFilter("XML Files", "*.xml"),
          new ExtensionFilter("All Files", "*.*"))
      }

      val file = fileChooser.showOpenDialog(window)
      if (file != null) {
        Game.mapPath = file
      }
    }
  }
  private var quitBtn = sceneSwitchButton("Quit", MainStage.States.Quit)

  private var title = new Label("Welcome to Tynooc") {
    padding = Insets(10.0)
    styleClass.add("big-label")
  }

  stylesheets += this.getClass.getResource("/css/main.css").toExternalForm
  stylesheets += this.getClass.getResource("/css/menu.css").toExternalForm

  root = new VBox(10.0) {
    alignment = Pos.Center
    padding = Insets(20.0)
    children = List(
      title,
      gameBtn,
      mapBtn,
      quitBtn)
  }

  /** Creates a button to switch to another scene. */
  private def sceneSwitchButton(text: String, newScene: MainStage.States.Val): Button = {
    var button: Button = new Button(text)
    button.onAction = (event: ActionEvent) => {
      sceneModifier(newScene)
    }
    button
  }
}
