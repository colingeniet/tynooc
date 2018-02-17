/* Game top menu */
import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.event._
import scalafx.scene.layout._
import scalafx.geometry._

class MenuPane(quitBtn: Button) extends HBox {
  children = List(quitBtn)
}
