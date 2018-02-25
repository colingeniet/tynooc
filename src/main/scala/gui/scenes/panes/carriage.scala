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
  private var name: Label = new Label(carriage.model.name + " carriage")
  private var health: Label = new Label()
  private var model: VBox = new CarriageModelStats(carriage.model)

  children = List(name, health, model)
  spacing = 3
  draw()

  override def draw(): Unit = {
    health.text = "health : " + carriage.health
  }
}

/** Engine detailed info display.
 *
 *  @param engine the engine to display.
 */
class EngineDetail(engine: Engine) extends DrawableVBox {
  private var name: Label = new Label(engine.model.name + " engine")
  private var health: Label = new Label()
  private var fuel: Label = new Label()
  private var model: VBox = new EngineModelStats(engine.model)

  children = List(name, health, fuel, model)
  spacing = 3
  draw()

  override def draw(): Unit = {
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
    new Label("comfort : " + model.comfort)
  )
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
    new Label("consum. : " + model.consumption)
  )
}
