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
import gui.scenes.panes.vehicle._
import gui.scenes.elements._
import logic.world._
import logic.town._
import logic.route._
import logic.vehicle._
import logic.vehicle.train._
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
extends MainStage.Scene(sceneModifier) {
  // panes contents
  private var top: TopMenu = new TopMenu(sceneModifier)
  private var left: VBox = new CompanyInfo(
    player.company,
    world,
    displayTrain,
    displayVehicleUnit)
  // empty by default
  private var right: VBox = new VBox()
  private var bottom: HBox = new HBox()
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

  private def displayTrain(train: Engine): Unit = {
    right = new TrainDetail(train)
    pane.right = right
  }

  private def displayTravel(travel: Travel): Unit = {
    right = new TravelInfo(travel)
    pane.right = right
  }

  private def displayVehicleUnit(vehicle: VehicleUnit): Unit = {
    right = VehicleUnitDetail(vehicle)
    pane.right = right
  }
}
