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
import gui.scenes.panes.facility._
import gui.scenes.elements._
import logic.world._
import logic.town._
import logic.route._
import logic.vehicle._
import logic.travel._
import logic.game._
import logic.facility._

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
  private var map = new Map(
    world,
    player.company,
    displayTown,
    displayRoute,
    displayTravel)
  private var messagesBox = new MessagesBox(5)

  // set content
  private var pane: BorderPane = new BorderPane {
    center = new StackPane {
      children = List(map, messagesBox)
    }
    top = new TopMenu(sceneModifier)
  }
  displayCompany()

  root = pane

  stylesheets += this.getClass.getResource("/css/main.css").toExternalForm
  stylesheets += this.getClass.getResource("/css/game.css").toExternalForm

  /* Content display methods */
  private def displayCompany(): Unit = {
    pane.left = new CompanyInfo(player.company, world, displayVehicle)
  }

  private def displayTown(town: Town): Unit = {
    pane.left = new VBox {
      children = List(
        new Button("Company") {
          onAction = (event: ActionEvent) => displayCompany
        },
        new Separator(),
        new TownInfo(town, displayRoute, displayFacility))
    }
  }

  private def displayRoute(route: Route): Unit = {
    pane.right = new RouteInfo(route, displayTown)
  }

  private def displayTravel(travel: Travel): Unit = {
    pane.right = new TravelInfo(travel)
  }

  private def displayVehicle(vehicle: VehicleUnit): Unit = {
    pane.right = VehicleUnitDetail(vehicle)
  }

  private def displayFacility(facility: Facility): Unit = {
    pane.right = FacilityDetail(facility)
  }

  private def printMessage(message: String): Unit = {
    messagesBox.print(message)
  }

  Game.printMessage = this.printMessage
}
