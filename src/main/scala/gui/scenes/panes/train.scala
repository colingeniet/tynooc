package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry._

import gui.scenes.elements.Link
import logic.train._

class Train(train: logic.train.Train) extends VBox {
  // list of carriages
  var trainDisplay: ScrollPane = new ScrollPane {
    content = new VBox(3) {
      children = List(
        new Engine(train.engine, displayEngine),
        new Separator(),
      )
      train.carriages.foreach { carriage =>
        children.add(new Carriage(carriage, displayCarriage))
      }
    }
  }
  var trainDetail: TrainDetail = new TrainDetail(train)

  children = List(trainDetail, trainDisplay)

  def displayEngine(engine: logic.train.Engine): Unit = {
    children = List(trainDetail, trainDisplay, new EngineDetail(engine))
  }

  def displayCarriage(carriage: logic.train.Carriage): Unit = {
    children = List(trainDetail, trainDisplay, new CarriageDetail(carriage))
  }
}


abstract class Element extends VBox(3) {
  update()
  def update(): Unit
}

class Engine(engine: logic.train.Engine, displayEngine: logic.train.Engine => Unit)
extends Element {
  def update(): Unit = {
    children = List(new Link("Engine")(displayEngine(engine)))
  }
}

class Carriage(carriage: logic.train.Carriage, displayCarriage: logic.train.Carriage => Unit)
extends Element {
  def update(): Unit = {
    children = List(new Link("Carriage")(displayCarriage(carriage)))
  }
}

class EngineDetail(engine: logic.train.Engine) extends Element {
  def update(): Unit = {
    children = List(new Label("Engine detail"))
  }
}

class CarriageDetail(carriage: logic.train.Carriage) extends Element {
  def update(): Unit = {
    children = List(new Label("Carriage detail"))
  }
}

class TrainDetail(train: logic.train.Train) extends Element {
  def update(): Unit = {
    children = List(new Label("Train detail"))
  }
}
