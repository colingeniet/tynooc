package gui.scenes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.event._
import scalafx.geometry._

import gui.MainStage

/** Option menu.
 *
 *  Currently unused.
 */
class Options(sceneModifier: MainStage.States.Val => Unit)
extends MainStage.Scene(sceneModifier) {
  private var menuBtn = new Button("Main Menu")
  menuBtn.onAction = (event: ActionEvent) => {
    sceneModifier(MainStage.States.MainMenu)
  }

  stylesheets += this.getClass.getResource("/css/main.css").toExternalForm
  stylesheets += this.getClass.getResource("/css/menu.css").toExternalForm

  root = new VBox(10) {
    alignment = Pos.Center
    children = List(
      new Label("Options") {
        styleClass.add("big-label")
      },
      menuBtn)
  }
}
