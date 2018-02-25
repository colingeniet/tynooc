package gui.scenes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.event._
import scalafx.scene.layout._
import scalafx.geometry._

import gui.MainStage
import gui.draw._
import gui.scenes.map._
import gui.scenes.panes._
import gui.scenes.elements._
import logic.world.World

//TEMPORARY
import logic.train._

class Game(sceneModifier: MainStage.States.Val=>Unit)
extends MainStage.Scene(sceneModifier) with Drawable {
  private var menuBtn: Button = new Button("Menu")
  private var _world: World = new World

  // panes contents
  private var top: DrawableHBox = new panes.Menu(menuBtn)
  // empty by default
  private var left: DrawableVBox = new Player()
  private var right: DrawableVBox = new TrainDetail(
    new Train(new Engine("Basic"), List(new Carriage("Basic"), new Carriage("Advanced")))
  )
  private var bottom: DrawableHBox = new DrawableHBox()
  private var center: Map = new Map(world, displayTown, displayRoute)

  private var pane: BorderPane = new BorderPane(
    center,
    top,
    right,
    bottom,
    left
  )

  root = pane

  menuBtn.onAction = (event: ActionEvent) => {
    sceneModifier(MainStage.States.MainMenu)
  }

  stylesheets += this.getClass.getResource("/css/main.css").toExternalForm

  private def displayTown(town: World.Town): Unit = {
    bottom = new panes.TownInfo(town, displayRoute)
    pane.bottom = bottom
  }

  private def displayRoute(route: World.Route): Unit = {
    bottom = new panes.RouteInfo(route, displayTown)
    pane.bottom = bottom
  }

  def world: World = _world

  def world_=(newWorld: World): Unit = {
    _world = newWorld
    pane.center = new ScrollPane {
      content = new Map(world, displayTown, displayRoute)
      // disable scroll
      vmax = 0
      hmax = 0
      hbarPolicy = ScrollPane.ScrollBarPolicy.Never
      vbarPolicy = ScrollPane.ScrollBarPolicy.Never
    }
  }

  override def draw(): Unit = {
    center.draw()
    top.draw()
    left.draw()
    right.draw()
    bottom.draw()
  }
}
