import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.event.ActionEvent
import scalafx.scene.layout.StackPane
import scalafx.geometry.Insets

class OptionsScene(sceneModifier: MainStage.States.Val=>Unit)
extends MainStage.Scene(sceneModifier) {
  private var menuBtn = new Button("Main Menu")
  menuBtn.onAction = (event: ActionEvent) => {
    sceneModifier(MainStage.States.MainMenu)
  }

  root = new StackPane {
    padding = Insets(20)
    children = List(menuBtn)
  }
}
