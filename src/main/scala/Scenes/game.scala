import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.event.ActionEvent
import scalafx.scene.layout.StackPane
import scalafx.geometry.Insets

class GameScene(sceneModifier: DisplayStates.Val=>Unit)
extends DisplayScene(sceneModifier) {
  private var menuBtn = new Button("Menu")
  menuBtn.onAction = (event: ActionEvent) => {
    sceneModifier(DisplayStates.MainMenu)
  }

  root = new StackPane {
    padding = Insets(20)
    children = List(menuBtn)
  }
}
