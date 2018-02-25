package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._
import scalafx.geometry._

import gui.draw._
import gui.scenes.elements.Link
import gui.scenes.panes._
import logic.train._

/** Train display panel.
 *
 *  @param train the train to display.
 */
class TrainDetail(train: Train) extends DrawableVBox {
  private var buttonGroup: ToggleGroup = new ToggleGroup

  // list of carriages
  private var carriagesList: List[RadioButton] =
    new RadioButton(train.engine.model.name + " engine") {
      onAction = (event: ActionEvent) => displayEngine(train.engine)
      styleClass.remove("radio-button")
      styleClass.add("link")
    } ::
    train.carriages.map(carriage =>
      new RadioButton(carriage.model.name + " carriage") {
        onAction = (event: ActionEvent) => displayCarriage(carriage)
        styleClass.remove("radio-button")
        styleClass.add("link")
      }
    )

  carriagesList.foreach(buttonGroup.toggles.add(_))

  // scroll pane containinng the list of carriages
  private var carriagesPane: ScrollPane = new ScrollPane {
    content = new VBox(3) {
      children = carriagesList
    }
  }

  // train statistics
  private var stats: TrainStats = new TrainStats(train)
  private var trainLink: RadioButton = new RadioButton("Train") {
    onAction = (event: ActionEvent) => displayTrain()
    styleClass.remove("radio-button")
    styleClass.add("link")
  }
  buttonGroup.toggles.add(trainLink)
  private var sep: Separator = new Separator()

  displayTrain()
  spacing = 3

  private def displayEngine(engine: Engine): Unit = {
    children = List(
      trainLink,
      carriagesPane,
      sep,
      new EngineDetail(engine)
    )
  }

  private def displayCarriage(carriage: Carriage): Unit = {
    children = List(
      trainLink,
      carriagesPane,
      sep,
      new CarriageDetail(carriage)
    )
  }

  private def displayTrain(): Unit = {
    children = List(
      trainLink,
      carriagesPane,
      sep,
      stats,
    )
  }

  override def draw(): Unit = {
    stats.draw()
  }
}

class TrainStats(train: Train) extends DrawableVBox {
  children = List(new Label("Train stats"))
}
