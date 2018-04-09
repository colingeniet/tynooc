package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.image._
import scalafx.event._

import gui.MainStage
import formatter._
import logic.game._

/** Game top menu bar. */
class TopMenu(sceneModifier: MainStage.States.Val => Unit)
extends BorderPane {
  // left side : quit button
  left = new HBox {
    children = List(
      new Button ("Quit") {
        onAction =
          (event: ActionEvent) => sceneModifier(MainStage.States.MainMenu)
      }
    )
  }

  // center, currently unused
  center = new HBox()

  // right side : time control
  /** Current time. */
  val timeLabel = new Label {
    text <== createStringBinding(
      () => TimeFormatter.timeToDateString(Game.time()),
      Game.time)
  }

  // control buttons
  val pauseButton = new ToggleButton {
    graphic = new ImageView {image = new Image(this, "/icons/pause.png")}
    onAction = (event: ActionEvent) => Game.paused = selected()
  }

  val timeGroup: ToggleGroup = new ToggleGroup()
  val buttonX1 = new RadioButton {
    graphic = new ImageView {image = new Image(this, "/icons/time1.png")}
    onAction = (event: ActionEvent) => Game.timeAcceleration = 1
    styleClass.remove("radio-button")
    styleClass.add("toggle-button")
    fire()
  }
  val buttonX2 = new ToggleButton {
    graphic = new ImageView {image = new Image(this, "/icons/time2.png")}
    onAction = (event: ActionEvent) => Game.timeAcceleration = 2.5
    styleClass.remove("radio-button")
    styleClass.add("toggle-button")
  }
  val buttonX3 = new ToggleButton {
    graphic = new ImageView {image = new Image(this, "/icons/time3.png")}
    onAction = (event: ActionEvent) => Game.timeAcceleration = 8
    styleClass.remove("radio-button")
    styleClass.add("toggle-button")
  }
  timeGroup.toggles.addAll(buttonX1, buttonX2, buttonX3)

  right = new HBox(3) {
    children = List(
      timeLabel,
      pauseButton,
      buttonX1,
      buttonX2,
      buttonX3)
  }
}
