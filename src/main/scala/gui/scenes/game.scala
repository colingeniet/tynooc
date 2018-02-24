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
import gui.scenes.elements._
import logic.world.World

class Game(sceneModifier: MainStage.States.Val=>Unit)
extends MainStage.Scene(sceneModifier) {
  private var menuBtn: Button = new Button("Menu")
  private var _world: World = new World

  private var pane: BorderPane = new BorderPane()
  root = pane
  pane.top = new panes.Menu(menuBtn)
  // empty by default
  pane.bottom = new Pane()
  pane.left = new Pane()
  pane.right = new Pane()
  pane.center = new Pane()

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
    pane.center = new ScrollPane {
      content = new map.Map(world, displayTown, displayRoute)
      // disable scroll
      vmax = 0
      hmax = 0
      hbarPolicy = ScrollPane.ScrollBarPolicy.Never
      vbarPolicy = ScrollPane.ScrollBarPolicy.Never
    }
  }
}
