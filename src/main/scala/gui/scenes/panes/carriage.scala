package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry._

import gui.draw._
import gui.scenes.elements.Link
import logic.train._


class CarriageDetail(carriage: logic.train.Carriage) extends DrawableVBox {
  children = List(new Label("Carriage detail"))
  spacing = 3
}

class EngineDetail(engine: logic.train.Engine) extends DrawableVBox {
  children = List(new Label("Engine detail"))
  spacing = 3
}

class CarriageShort(
  carriage: logic.train.Carriage,
  displayCarriage: logic.train.Carriage => Unit
)
extends DrawableVBox {
  children = List(new Link("Carriage short")(displayCarriage(carriage)))
  spacing = 3
}

class EngineShort(
  engine: logic.train.Engine,
  displayEngine: logic.train.Engine => Unit
)
extends DrawableVBox {
  children = List(new Link("Engine short")(displayEngine(engine)))
  spacing = 3
}
