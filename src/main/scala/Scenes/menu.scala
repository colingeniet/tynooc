import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.event.ActionEvent
import scalafx.scene.layout.VBox
import scalafx.geometry._

import scalafx.geometry.Insets

class MainMenuScene(sceneModifier: DisplayStates.Val=>Unit)
extends DisplayScene(sceneModifier) {
  private var gameBtn = sceneSwitchButton("Play", DisplayStates.Game)
  private var optionsBtn = sceneSwitchButton("Options", DisplayStates.Options)
  private var quitBtn = sceneSwitchButton("Quit", DisplayStates.Quit)

  root = new VBox(10.0) {
    alignment = Pos.CENTER
    padding = Insets(50)
    children = List(gameBtn, optionsBtn, quitBtn)
  }


  def sceneSwitchButton(text: String, newScene: DisplayStates.Val): Button = {
    var button: Button = new Button(text)
    button.onAction = (event: ActionEvent) => {
      sceneModifier(newScene)
    }
    button
  }
}
