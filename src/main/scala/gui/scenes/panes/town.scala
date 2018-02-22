package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._

import world._

class Town extends HBox {
  children = List(new Label("Town pane"))

  def displayTown(town: World.Town) {
    children = List(new Label(town.name))
  }

  def displayRoute(route: World.Route) {
    children = List(new Label(route.start.name), new Label(route.end.name))
  }
}
