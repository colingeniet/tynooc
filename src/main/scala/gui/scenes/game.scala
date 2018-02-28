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
import logic.world._
import logic.town._
import logic.route._

//TEMPORARY
import logic.train._
import logic.player._

class Game(val world: World, sceneModifier: MainStage.States.Val=>Unit)
extends MainStage.Scene(sceneModifier) with Drawable {
  private var menuBtn: Button = new Button("Menu")

  // panes contents
  private var top: DrawableHBox = new panes.Menu(menuBtn)

  private var player: Player = new Player()
  player.addMoney(100)
  private var left: DrawableVBox = new PlayerInfo(
    player,
    world,
    displayTrain,
    displayEngine,
    displayCarriage)

  private var right: DrawableVBox = new DrawableVBox()
  private var bottom: DrawableHBox = new DrawableHBox()
  private var center: Map = new Map(world, displayTown, displayRoute)

  private var pane: BorderPane = new BorderPane(
    center,
    top,
    right,
    bottom,
    left)

  root = pane

  menuBtn.onAction = (event: ActionEvent) => {
    sceneModifier(MainStage.States.MainMenu)
  }

  stylesheets += this.getClass.getResource("/css/main.css").toExternalForm

  private def displayTown(town: Town): Unit = {
    bottom = new TownInfo(town, displayRoute)
    pane.bottom = bottom
  }

  private def displayRoute(route: Route): Unit = {
    bottom = new RouteInfo(route, displayTown)
    pane.bottom = bottom
  }

  private def displayTrain(train: Train): Unit = {
    right = new TrainDetail(train)
    pane.right = right
  }

  private def displayEngine(engine: Engine): Unit = {
    right = new EngineDetail(engine)
    pane.right = right
  }

  private def displayCarriage(carriage: Carriage): Unit = {
    right = new CarriageDetail(carriage)
    pane.right = right
  }

  override def draw(): Unit = {
    center.draw()
    top.draw()
    left.draw()
    right.draw()
    bottom.draw()
  }
}
