package gui.scenes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.event._
import scalafx.scene.layout._
import scalafx.geometry._

import gui.MainStage
import gui.scenes.map._
import gui.scenes.panes._
import world.World

class Game(sceneModifier: MainStage.States.Val=>Unit)
extends MainStage.Scene(sceneModifier) {
  private var menuBtn: Button = new Button("Menu")
  private var _world: World = new World

  private var pane: BorderPane = new BorderPane()
  root = pane
  pane.top = new panes.Menu(menuBtn)
  pane.bottom = new Pane()    // empty by default
  pane.left = new panes.Player()
  pane.right = new Pane()     // empty by default
  pane.center = new map.Map(world, displayTown, displayRoute)

  menuBtn.onAction = (event: ActionEvent) => {
    sceneModifier(MainStage.States.MainMenu)
  }

  stylesheets += this.getClass.getResource("/css/main.css").toExternalForm


  def displayTown(town: World.Town): Unit = {
    pane.bottom = new panes.Town(town, displayRoute)
  }

  def displayRoute(route: World.Route): Unit = {
    pane.bottom = new panes.Route(route, displayTown)
  }

  def world: World = _world

  def world_=(newWorld: World): Unit = {
    _world = newWorld
    pane.center = new map.Map(world, displayTown, displayRoute)
  }
}
