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
import gui.scenes.panes.vehicle._
import logic.vehicle._

/** Train display panel.
 *
 *  @param train the train to display.
 */
class TrainDetail(train: Engine) extends VBox(3) {
  private val list: SelectionListDynamic[Carriage] = new SelectionListDynamic[Carriage](
    train.carriages,
    c => new StringProperty(c.model.name),
    displayCarriage(_))

  // train statistics
  private val stats: TrainStats = new TrainStats(train)
  private val sep1: Separator = new Separator()
  private val sep2: Separator = new Separator()
  // bottom panel for detailed statistics
  private var detail: VBox = new VBox()

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
    detail = VehicleUnitDetail(engine)
    setChildren()
  }

  /** Display carriage detail in the lower panel */
  private def displayCarriage(carriage: Carriage): Unit = {
    detail = VehicleUnitDetail(carriage)
    setChildren()
  }
}

/** General train statistics. */
class TrainStats(train: Engine) extends VBox {
  private val name: Label = new Label()
  private val status: Label = new Label()
  private val tooHeavy: Label = new Label("Too heavy !") {
    styleClass.add("alert")
  }
  private val weight: Label = new Label()
  private val power: Label = new Label("power : " + train.model.power)

  children = List(name, status, weight, power)
  spacing = 3

  name.text <== train.name

  status.text <== Bindings.createStringBinding(
    () => (if (train.onTravel()) "en route to " else "stored at ")
      + train.town().name,
    train.onTravel,
    train.town)

  weight.text <== createStringBinding(
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
