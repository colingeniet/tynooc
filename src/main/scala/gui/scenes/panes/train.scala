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
  // button group - only select one carriage at a time
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

  // add all newly created buttons to the group
  carriagesList.foreach(buttonGroup.toggles.add(_))

  // scroll pane containinng the list of carriages
  private var carriagesPane: ScrollPane = new ScrollPane {
    content = new VBox(3) {
      children = carriagesList
    }
    hbarPolicy = ScrollPane.ScrollBarPolicy.Never
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
  // bottom panel for detailed statistics
  private var detail: DrawableVBox = new DrawableVBox()

  displayTrain()
  spacing = 3


  private def setChildren(): Unit = {
    children = List(
      trainLink,
      carriagesPane,
      sep,
      detail
    )
  }

  /** Display engine detail in the lower panel */
  private def displayEngine(engine: Engine): Unit = {
    detail = new EngineDetail(engine)
    setChildren()
  }

  /** Display carriage detail in the lower panel */
  private def displayCarriage(carriage: Carriage): Unit = {
    detail = new CarriageDetail(carriage)
    setChildren()
  }

  /** Display train detail in the lower panel */
  private def displayTrain(): Unit = {
    detail = stats
    setChildren()
  }

  override def draw(): Unit = {
    detail.draw()
  }
}

class TrainStats(train: Train) extends DrawableVBox {
  private var weight: Label = new Label()
  private var power: Label = new Label()

  children = List(weight, power)
  spacing = 3
  draw()

  override def draw(): Unit = {
    weight.text = "weight : " + train.weight
    power.text = "power : " + train.engine.model.power
  }
}
