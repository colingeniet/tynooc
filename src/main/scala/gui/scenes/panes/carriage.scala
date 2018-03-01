package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry._

import gui.draw._
import gui.scenes.elements.Link
import logic.train._


/** Carriage detailed info display.
 *
 *  @param carriage the carriage to display.
 */
class CarriageDetail(carriage: Carriage) extends DrawableVBox {
  private val name: Label = new Label(carriage.model.name + " carriage")
  private val status: Label = new Label()
  private val health: Label = new Label()
  private val model: VBox = new CarriageModelStats(carriage.model)

  children = List(name, status, health, model)
  spacing = 3
  draw()

  override def draw(): Unit = {
    if(carriage.isUsed) {
      status.text = "in train"
    } else {
      status.text = "stored at " + carriage.town.name
    }
    health.text = "health : " + carriage.health
  }
}

/** Engine detailed info display.
 *
 *  @param engine the engine to display.
 */
class EngineDetail(engine: Engine) extends DrawableVBox {
  private val name: Label = new Label(engine.model.name + " engine")
  private val status: Label = new Label()
  private val health: Label = new Label()
  private val fuel: Label = new Label()
  private val model: VBox = new EngineModelStats(engine.model)

  children = List(name, status, health, fuel, model)
  spacing = 3
  draw()

  override def draw(): Unit = {
    if(engine.isUsed) {
      status.text = "in train"
    } else {
      status.text = "stored at " + engine.town.name
    }
    health.text = "health : " + engine.health
    fuel.text = "fuel : " + engine.fuel
  }
}


/** Carriage model info display.
 *
 *  @param model the carriage model to display.
 */
class CarriageModelStats(model: CarriageModel) extends VBox(3) {
  children = List (
    new Label("cap. : " + model.capacity),
    new Label("weight : " + model.weight),
    new Label("comfort : " + model.comfort))
}

/** Engine model info display.
 *
 *  @param model the engine model to display.
 */
class EngineModelStats(model: EngineModel) extends VBox(3) {
  children = List (
    new Label("speed : " + model.speed),
    new Label("power : " + model.power),
    new Label("weight : " + model.weight),
    new Label("fuel cap. : " + model.fuelCapacity),
    new Label("consum. : " + model.consumption))
}
