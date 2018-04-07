package gui

import scalafx.Includes._
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert._
import scalafx.stage.Modality
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.application.Platform
import scalafx.scene.media._

import gui.scenes._
import logic.world._
import logic.game._
import ai._
import player._
import parser._

import java.io.File
import scala.util.Try


/** Main window manager.
 *
 *  Handles, displays and switch between menus and game screens.
 */
class MainStage(gameInit: () => Player)
extends JFXApp.PrimaryStage {
  /* Actual scenes displayed. */
  private var mainMenuScene: MainMenu = new MainMenu(changeScene)
  private var gameScene: Game = null

  private var onNextChangeCallback: () => Unit = () => ()

  /* Stage configuration. */
  title.value = "Tynooc"
  scene = mainMenuScene
  width = 1024
  height = 720

  /** Changes the scene displayed.
   *
   *  @param newScene the scene to switch to.
   */
  def changeScene(newScene: MainStage.States.Val): Unit = {
    // call then reset callback
    onNextChangeCallback()
    onNextChangeCallback = () => ()

    // switch scene
    newScene match {
      case MainStage.States.MainMenu => scene = mainMenuScene
      case MainStage.States.Game => {
        try {
          var mainPlayer: Player = gameInit()
          gameScene = new Game(Game.world, mainPlayer, changeScene)
          scene = gameScene

          // launch background game loop when the game scene is selected
          val mainLoopThread: Thread = new Thread {
            override def run {
              while(true) {
                Platform.runLater(() => {
                  Game.update()
                })
                Thread.sleep(33)
              }
            }
          }
          mainLoopThread.start()

          // kill background thread when leaving
          onNextChangeCallback = () => mainLoopThread.stop()
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
      case MainStage.States.Quit => Platform.exit()
    }
  }

  // run callback on exit
  def onExit(): Unit = {
    onNextChangeCallback()
  }
}

/** Main window manager companion object.
 */
object MainStage {
  /** Scenes available to display by the MainStage.
   */
  object States {
    sealed trait Val
    case object MainMenu extends Val
    case object Game     extends Val
    case object Quit     extends Val
  }

  /** The type of a scene manipulated by the MainStage.
   *
   *  @param sceneModifier a callback that can be used to switch to
   *    a different scene.
   */
  abstract class Scene(sceneModifier: States.Val=>Unit)
  extends scalafx.scene.Scene
}

object Resources {
  val SoundPath: String = "src/main/resources/audio/clic.mp3"
  val Sound = Try{ new AudioClip(new File(SoundPath).toURI().toString()) }.toOption
}
