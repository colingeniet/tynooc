package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._

import gui.draw._
import gui.MainStage
import logic.game._

class TopMenu(sceneModifier: MainStage.States.Val => Unit)
extends BorderPane with Drawable {
  left = new HBox {
    children = List(
      new Button ("Quit") {
        onAction =
          (event: ActionEvent) => sceneModifier(MainStage.States.MainMenu)
      }
    )
  }
  center = new HBox {
    children = List()
  }

  val timeGroup: ToggleGroup = new ToggleGroup()
  val buttonX1 = new ToggleButton ("x1") {
    onAction = (event: ActionEvent) => Game.timeAcceleration = 1
    fire()
  }
  val buttonX2 = new ToggleButton ("x2") {
    onAction = (event: ActionEvent) => Game.timeAcceleration = 2
  }
  val buttonX4 = new ToggleButton ("x4") {
    onAction = (event: ActionEvent) => Game.timeAcceleration = 4
  }
  timeGroup.toggles.addAll(buttonX1, buttonX2, buttonX4)

  right = new HBox {
    children = List(
      new ToggleButton ("Pause") {
        onAction = (event: ActionEvent) => Game.paused = selected()
      },
      buttonX1,
      buttonX2,
      buttonX4)
  }
}
