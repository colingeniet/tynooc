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

  private var menuPane = new panes.Menu(menuBtn)
  private var trainPane = new panes.Train
  private var townPane = new panes.Town
  private var playerPane = new panes.Player
  private var mapDisplay = new map.Map(world)

  private var pane: BorderPane = new BorderPane(
    mapDisplay,
    menuPane,
    trainPane,
    townPane,
    playerPane
  )

  menuBtn.onAction = (event: ActionEvent) => {
    sceneModifier(MainStage.States.MainMenu)
  }

  stylesheets += this.getClass.getResource("/css/main.css").toExternalForm

  root = pane

  def world: World = _world

  def world_=(newWorld: World): Unit = {
    _world = newWorld
    pane.center = new map.Map(world)
  }
}
