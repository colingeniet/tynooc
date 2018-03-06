package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._
import scalafx.geometry._
import scalafx.scene.paint.Color

import gui.draw._
import gui.scenes.elements._
import gui.scenes.panes._
import logic.train._

/** Train display panel.
 *
 *  @param train the train to display.
 */
class TrainDetail(train: Train) extends DrawableVBox {
  private val list: SelectionMenu = new SelectionMenu()
  list.addMenu(
    train.engine.model.name + " engine",
    displayEngine(train.engine))
  train.carriages.reverse.foreach(carriage =>
    list.addMenu(carriage.model.name + " carriage", displayCarriage(carriage)))

  // train statistics
  private val stats: TrainStats = new TrainStats(train)
  private val sep1: Separator = new Separator()
  private val sep2: Separator = new Separator()
  // bottom panel for detailed statistics
  private var detail: DrawableVBox = new DrawableVBox()

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

  override def draw(): Unit = {
    stats.draw()
    detail.draw()
  }
}

/** General train statistics. */
class TrainStats(train: Train) extends DrawableVBox {
  private val name: Label = new Label()
  private val status: Label = new Label()
  private val tooHeavy: Label = new Label("Too heavy !") {
    styleClass.add("alert")
  }
  private val damaged: Label = new Label("damaged !") {
    styleClass.add("alert")
  }
  private val weight: Label = new Label()
  private val power: Label = new Label()

  children = List(name, status, weight, power)
  spacing = 3
  draw()

  override def draw(): Unit = {
    name.text = train.name
    status.text =
      (if (train.onRoute) "en route to " else "stored at ") + train.town.name
    weight.text = "weight : " + train.weight
    power.text = "power : " + train.engine.model.power

    children = List(
      Some(name),
      Some(status),
      (if (train.tooHeavy) Some(tooHeavy) else None),
      Some(weight),
      (if (train.isDamaged) Some(damaged) else None),
      Some(power)).flatMap(x => x)
  }
}
