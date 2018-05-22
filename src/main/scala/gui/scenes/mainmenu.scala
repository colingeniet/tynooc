package gui.scenes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._
import scalafx.geometry._
import scalafx.stage._
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert._
import scalafx.scene.paint.Color
import javafx.stage.FileChooser.ExtensionFilter

import gui.MainStage
import gui.scenes.color._
import parser._
import logic.game._

import java.io._

/** Game main menu.
 */
class MainMenu(
  window: Window,
  sceneModifier: MainStage.States.Val => Unit,
  gameInit: () => Unit)
extends MainStage.Scene(sceneModifier) {
  private var gameBtn = new Button("Play") {
    onAction = (event: ActionEvent) => {
      try {
        // game initialization
        gameInit()
        Colors.init(Game.mainPlayer.get.company)
        sceneModifier(MainStage.States.Game)
      } catch {
        case e: java.io.FileNotFoundException => {
          new Alert(AlertType.Error) {
            title = "file error"
            headerText = "Map file not found"
            contentText = e.getMessage()
            initModality(Modality.None)
          }.show()
        }
        case e: BadFileFormatException => {
          new Alert(AlertType.Error) {
            title = "file error"
            headerText = "Invalid map file"
            contentText = e.getMessage()
            initModality(Modality.None)
          }.show()
        }
      }
    }
  }

  private var loadLastBtn = new Button("Load last save") {
    onAction = (event: ActionEvent) => {
      // game initialization
      val stream = new ObjectInputStream(new FileInputStream("autosave"))
      Game.load_game(stream)
      stream.close()
      Colors.init(Game.mainPlayer.get.company)
      sceneModifier(MainStage.States.Game)
    }
  }

  private var loadBtn = new Button("Load save") {
    onAction = (event: ActionEvent) => {
      val fileChooser = new FileChooser {
        title = "open save file"
        extensionFilters ++= Seq(
          new ExtensionFilter("All Files", "*"))
      }

      val file = fileChooser.showOpenDialog(window)
      if (file != null) {
        // game initialization
        try {
          val stream = new ObjectInputStream(new FileInputStream(file))
          Game.load_game(stream)
          stream.close()
          Colors.init(Game.mainPlayer.get.company)
          sceneModifier(MainStage.States.Game)
        } catch {
          case e: java.io.IOException => {
            new Alert(AlertType.Error) {
              title = "file error"
              headerText = "Invalid save file"
              contentText = e.getMessage()
              initModality(Modality.None)
            }.show()
          }
        }
      }
    }
  }

  private var mapBtn = new Button("Select Map") {
    onAction = (event: ActionEvent) => {
      val fileChooser = new FileChooser {
        title = "open map file"
        initialDirectory = Game.mapPath.getParentFile()
        extensionFilters ++= Seq(
          new ExtensionFilter("XML Files", "*.xml"),
          new ExtensionFilter("All Files", "*"))
      }

      val file = fileChooser.showOpenDialog(window)
      if (file != null) {
        Game.mapPath = file
      }
    }
  }
  private var quitBtn = new Button("Quit") {
    onAction = (event: ActionEvent) => {
      sceneModifier(MainStage.States.Quit)
    }
  }

  private var title = new Label("Welcome to Tynooc") {
    padding = Insets(10.0)
    styleClass.add("big-label")
  }

  stylesheets += this.getClass.getResource("/css/main.css").toExternalForm
  stylesheets += this.getClass.getResource("/css/menu.css").toExternalForm

  root = new VBox(10.0) {
    alignment = Pos.Center
    padding = Insets(20.0)
    children = List(
      title,
      gameBtn,
      loadLastBtn,
      loadBtn,
      mapBtn,
      quitBtn)
  }
}
