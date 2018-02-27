package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._
import scalafx.geometry._

import gui.draw._
import gui.scenes.elements._
import gui.scenes.panes._
import logic.train._

/** Train display panel.
 *
 *  @param train the train to display.
 */
class TrainDetail(train: Train) extends DrawableVBox {
  private var list: SelectionMenu = new SelectionMenu()
  list.addMenu(
    train.engine.model.name + " engine",
    displayEngine(train.engine))
  train.carriages.foreach(carriage =>
    list.addMenu(carriage.model.name + " carriage", displayCarriage(carriage)))

  // train statistics
  private var stats: TrainStats = new TrainStats(train)
  private var sep1: Separator = new Separator()
  private var sep2: Separator = new Separator()
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

class TrainStats(train: Train) extends DrawableVBox {
  private var status: Label = new Label()
  private var weight: Label = new Label()
  private var power: Label = new Label()

  children = List(status, weight, power)
  spacing = 3
  draw()

  override def draw(): Unit = {
    status.text =
      (if (train.onRoute) "on route to " else "stored at ") + train.town.name
    weight.text = "weight : " + train.weight
    power.text = "power : " + train.engine.model.power
  }
}
