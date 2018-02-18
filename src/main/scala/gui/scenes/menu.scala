package gui.scenes

import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.control.Label
import scalafx.event.ActionEvent
import scalafx.scene.layout.VBox
import scalafx.geometry._
import scalafx.geometry.Insets

import gui.MainStage

class MainMenu(sceneModifier: MainStage.States.Val=>Unit)
extends MainStage.Scene(sceneModifier) {
  private var gameBtn = sceneSwitchButton("Play", MainStage.States.Game)
  private var optionsBtn = sceneSwitchButton("Options", MainStage.States.Options)
  private var quitBtn = sceneSwitchButton("Quit", MainStage.States.Quit)

  private var title = new Label("Welcome to Tynooc") {
    padding = Insets(10.0)
  }

  stylesheets += this.getClass.getResource("/css/main.css").toExternalForm

  root = new VBox(10.0) {
    alignment = Pos.CENTER
    padding = Insets(20.0)
    children = List(title, gameBtn, optionsBtn, quitBtn)
  }


  def sceneSwitchButton(text: String, newScene: MainStage.States.Val): Button = {
    var button: Button = new Button(text)
    button.onAction = (event: ActionEvent) => {
      sceneModifier(newScene)
    }
    button
  }
}
