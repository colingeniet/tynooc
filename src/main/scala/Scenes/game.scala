import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.event.ActionEvent
import scalafx.scene.layout.BorderPane
import scalafx.geometry.Insets

class GameScene(sceneModifier: DisplayStates.Val=>Unit)
extends DisplayScene(sceneModifier) {
  var button = new Button("tut")
  button.onAction = (event: ActionEvent) => {
    sceneModifier(DisplayStates.MainMenu)
  }

  root = new BorderPane {
    padding = Insets(25)
    center = button
  }
}
