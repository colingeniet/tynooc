package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._
import scalafx.geometry._
import scalafx.scene.paint.Color
import scalafx.beans.binding._
import scalafx.beans.property._
import scalafx.collections._
import scalafx.collections.ObservableBuffer._

import gui.scenes.elements._
import gui.scenes.panes._
import logic.vehicle._
import logic.vehicle.train._

/** Train display panel.
 *
 *  @param train the train to display.
 */
class TrainDetail(train: Train) extends VBox {
  private val list: ScrollPane = new ScrollPane {
    content = new SelectionMenu {
      addMenu(train.engine.model.name + " engine", displayEngine(train.engine))
      train.carriages.foreach { carriage =>
        addMenu(carriage.model.name + " carriage", displayCarriage(carriage))
      }

      train.carriages.onChange(
        (_: ObservableBuffer[Carriage], changes: Seq[Change[Carriage]]) => {
          changes.foreach(_ match {
            case Add(_, added) => added.foreach(c =>
              addMenu(c.model.name + " carriage", displayCarriage(c)))
            case Remove(pos, removed) => children.remove(pos, pos + removed.size)
            case _ => ()
          })
        })
    }
  }

  // train statistics
  private val stats: TrainStats = new TrainStats(train)
  private val sep1: Separator = new Separator()
  private val sep2: Separator = new Separator()
  // bottom panel for detailed statistics
  private var detail: VBox = new VBox()

  spacing = 3
  setChildren()

  private def setChildren(): Unit = {
    children = List(
      stats,
      sep1,
      list,
      sep2,
      detail)
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
}

/** General train statistics. */
class TrainStats(train: Train) extends VBox {
  private val name: Label = new Label()
  private val status: Label = new Label()
  private val tooHeavy: Label = new Label("Too heavy !") {
    styleClass.add("alert")
  }
  private val weight: Label = new Label()
  private val power: Label = new Label("power : " + train.engine.model.power)

  children = List(name, status, weight, power)
  spacing = 3

  name.text <== train.name

  status.text <== Bindings.createStringBinding(
    () => (if (train.onTravel()) "en route to " else "stored at ")
      + train.town().name,
    train.onTravel,
    train.town)

  weight.text <== Bindings.createStringBinding(
    () => "weight : " + train.weight(),
    train.weight)

  private def updateChildren(): Unit = {
    children = List(
      Some(name),
      Some(status),
      (if (train.tooHeavy()) Some(tooHeavy) else None),
      Some(weight),
      Some(power)).flatMap(x => x)
  }

  train.tooHeavy.onChange(updateChildren())
}
