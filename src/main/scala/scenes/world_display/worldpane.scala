import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.canvas._
import scalafx.event._
import scalafx.geometry._
import scalafx.scene.paint.Color._

class WorldPane extends Canvas(200, 200) {
  graphicsContext2D.fill = LightGreen
  graphicsContext2D.fillRect(50,50,100,100)
}
