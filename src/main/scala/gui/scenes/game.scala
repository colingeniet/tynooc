package gui.scenes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.event._
import scalafx.scene.layout._
import scalafx.geometry._

import gui.MainStage
import gui.scenes.world._
import gui.scenes.panes._

class Game(sceneModifier: MainStage.States.Val=>Unit)
extends MainStage.Scene(sceneModifier) {
  private var menuBtn = new Button("Menu")
  menuBtn.onAction = (event: ActionEvent) => {
    sceneModifier(MainStage.States.MainMenu)
  }

  stylesheets += this.getClass.getResource("/css/main.css").toExternalForm

  root = new BorderPane(
    new world.World,
    new panes.Menu(menuBtn),
    new panes.Train,
    new panes.Town,
    new panes.Player,
  )
}
