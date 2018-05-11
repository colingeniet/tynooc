package gui

import collection.mutable.HashMap

import scalafx.Includes._
import scalafx.stage.Modality
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.application.Platform
import scalafx.scene.media._
import scalafx.scene.image.Image

import gui.scenes._
import logic.world._
import logic.game._
import ai._
import player._
import parser._
import logic.vehicle._

import java.io._
import scala.util.Try


/** Main window manager.
 *
 *  Handles, displays and switch between menus and game screens.
 */
class MainStage(gameInit: () => Unit)
extends JFXApp.PrimaryStage {
  /* Actual scenes displayed. */
  private var mainMenuScene: MainMenu =
    new MainMenu(this, changeScene, gameInit)
  private var gameScene: Game = null

  private var onNextChangeCallback: () => Unit = () => ()
  /* Stage configuration. */
  title.value = "Tynooc"
  scene = mainMenuScene
  width = 1024
  height = 720

  Resources.load

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
        gameScene = new Game(this, Game.world, Game.mainPlayer.get, changeScene)
        scene = gameScene

        var runMainLoop: Boolean = true
        // launch background game loop when the game scene is selected
        val mainLoopThread: Thread = new Thread {
          override def run {
            while(runMainLoop) {
              Platform.runLater(() => {
                Game.update()
              })
              Thread.sleep(12)
            }
          }
        }
        // kill background thread and save when leaving
        onNextChangeCallback = () => {
          runMainLoop = false
          val stream = new ObjectOutputStream(new FileOutputStream("autosave"))
          Game.save_game(stream)
          stream.close()
        }
        mainLoopThread.start()
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

/* Object to manage resources. Sound doesn’t work with too old JDK version, so
   we delete it.*/
object Resources {
  val soundPath: String = "src/main/resources/audio/clic.mp3"

  /* It would be better to load these strings from a file. */
  val iconsPath: HashMap[String, String] = HashMap(
    "basic engine"      -> "src/main/resources/icons/train.png",
    "advanced engine"   -> "src/main/resources/icons/train.png",

    "basic truck"       -> "src/main/resources/icons/truck.png",
    "advanced truck"    -> "src/main/resources/icons/truck.png",
    "jeep"              -> "src/main/resources/icons/truck.png",
    "cheap truck"       -> "src/main/resources/icons/truck.png",

    "basic plane"       -> "src/main/resources/icons/plane.png",
    "advanced plane"    ->  "src/main/resources/icons/plane.png",
    "fast supply plane" ->  "src/main/resources/icons/plane.png",

    "basic ship"        -> "src/main/resources/icons/ship.png",
    "advanced ship"     -> "src/main/resources/icons/ship.png",

    "cargo"             -> "src/main/resources/icons/ship.png",
    "huge cargo"        -> "src/main/resources/icons/ship.png"
  )

  val icons = iconsPath.mapValues { p => new Image(new File(p).toURI().toString()) }

  val sound = Try{ new AudioClip(new File(soundPath).toURI().toString()) }.toOption

  def load: Unit = {
    if(sound == None)
      println("Impossible to load sound. Play will be without it.")
    if(icons.values.exists(_.error())) /* TODO : throw exception. */
      println("Impossible to load some icons")
  }

  def images(vehicle: Vehicle): Image = icons(vehicle.model.name)
}
