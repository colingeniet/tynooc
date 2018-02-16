import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.event.ActionEvent
import scalafx.scene.layout.BorderPane
import scalafx.geometry.Insets

class MainMenuScene(sceneModifier: DisplayStates.Val=>Unit)
extends DisplayScene(sceneModifier) {
  var button = new Button("boum")
  button.onAction = (event: ActionEvent) => {
    sceneModifier(DisplayStates.Game)
  }

  root = new BorderPane {
    padding = Insets(25)
    center = button
  }
}
