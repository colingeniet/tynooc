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
import logic.train._
import logic.travel._

import player._

/** Game scene.
 *
 *  Main game interface, contains the world map and side panels.
 *  @param world the world to be displayed on the map.
 *  @param player the player.
 */
class Game(
  val world: World,
  val player: Player,
  sceneModifier: MainStage.States.Val => Unit)
extends MainStage.Scene(sceneModifier) with Drawable {
  // panes contents
  private var top: TopMenu = new TopMenu(sceneModifier)
  private var left: DrawableVBox = new CompanyInfo(
    player.company,
    world,
    displayTrain,
    displayEngine,
    displayCarriage)
  // empty by default
  private var right: DrawableVBox = new DrawableVBox()
  private var bottom: DrawableHBox = new DrawableHBox()
  private var center: Map = new Map(world, player.company,displayTown, displayRoute,
                                    displayTravel)

  // set content
  private var pane: BorderPane = new BorderPane(
    center,
    top,
    right,
    bottom,
    left)
  root = pane

  stylesheets += this.getClass.getResource("/css/main.css").toExternalForm
  stylesheets += this.getClass.getResource("/css/game.css").toExternalForm

  /* Content display methods */

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

  private def displayTravel(travel: Travel): Unit = {
    right = new TravelInfo(travel)
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

  // update content
  override def draw(): Unit = {
    center.draw()
    top.draw()
    left.draw()
    right.draw()
    bottom.draw()
  }
}
