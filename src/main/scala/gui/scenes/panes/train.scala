package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
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
  // list of carriages
  private var carriagesShort: List[DrawableVBox] =
    new EngineShort(train.engine, displayEngine) ::
    train.carriages.map(new CarriageShort(_, displayCarriage))

  // scroll pane containinng the list of carriages
  private var carriagesPane: ScrollPane = new ScrollPane {
    content = new VBox {
      children = carriagesShort
    }
  }

  // train statistics
  private var stats: TrainStats = new TrainStats(train)
  private var trainLink: Link = new Link("Train")(displayTrain)
  private val sep1: Separator = new Separator()
  private val sep2: Separator = new Separator()
  displayTrain()
  spacing = 3

  private def displayEngine(engine: Engine): Unit = {
    children = List(
      trainLink,
      sep1,
      carriagesPane,
      sep2,
      new EngineDetail(engine)
    )
  }

  private def displayCarriage(carriage: Carriage): Unit = {
    children = List(
      trainLink,
      sep1,
      carriagesPane,
      sep2,
      new CarriageDetail(carriage)
    )
  }

  private def displayTrain(): Unit = {
    children = List(
      trainLink,
      sep1,
      carriagesPane,
      sep2,
      stats,
    )
  }

  override def draw(): Unit = {
    carriagesShort.foreach(_.draw())
    stats.draw()
  }
}

class TrainStats(train: Train) extends DrawableVBox {
  children = List(new Label("Train stats"))
}
