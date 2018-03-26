package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry._
import scalafx.beans.binding._

import gui.scenes.elements.Link
import gui.scenes.panes.vehicle._
import logic.vehicle._
import logic.vehicle.train._


/** Carriage detailed info display.
 *
 *  @param carriage the carriage to display.
 */
class CarriageDetail(carriage: Carriage) extends VBox {
  private val name: Label = new Label(carriage.model.name + " carriage")
  private val status: Label = new Label()
  private val model: VBox = new VehicleModelShortStats(carriage.model)

  spacing = 3
  children = List(name, status, model)
  status.text <== Bindings.createStringBinding(
    () => {
      if(carriage.isUsed()) "in " + carriage.train().get.name()
      else "stored at " + carriage.town().name
    },
    carriage.isUsed)
}

/** Engine detailed info display.
 *
 *  @param engine the engine to display.
 */
class EngineDetail(engine: Engine) extends VBox {
  private val name: Label = new Label(engine.model.name + " engine")
  private val model: VBox = new VehicleModelShortStats(engine.model)

  spacing = 3
  children = List(name, model)
}
