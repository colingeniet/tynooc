package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry._
import scalafx.beans.binding._

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
  private val model: VBox = new CarriageModelStats(carriage.model)

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
class EngineDetail(engine: Engine) extends DrawableVBox {
  private val name: Label = new Label(engine.model.name + " engine")
  private val status: Label = new Label()
  private val model: VBox = new EngineModelStats(engine.model)

  spacing = 3
  children = List(name, status, model)
  status.text <== Bindings.createStringBinding(
    () => {
      if(engine.isUsed()) "in " + engine.train().get.name()
      else "stored at " + engine.town().name
    },
    engine.isUsed)
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
    new Label("consum. : " + model.consumption))
}

/** Generic info display.
 *
 *  @param m the class with the info do display
 */
object Stats {
  def statFilter(field: java.lang.reflect.Field): Boolean = false
}

/** Generic info display.
 *
 *  @param m the class with the info do display
 */
class Stats[A](m: A) extends VBox(3) {
  children = m.getClass().getDeclaredFields.toList.map(a => {a.setAccessible(true); a}).filterNot(Stats.statFilter(_)).map {
    a => new Label(a.getName() + " : " + a.get(m))
  }
}
