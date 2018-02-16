import scalafx.Includes._
import scalafx.scene.Scene


object DisplayStates {
  sealed trait Val
  case object MainMenu extends Val
  case object GameMenu extends Val
  case object Game     extends Val
  case object Options  extends Val
  case object Quit     extends Val
}

abstract class DisplayScene(sceneModifier: DisplayStates.Val=>Unit) extends Scene
