package gui.scenes.world

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.canvas._
import scalafx.event._
import scalafx.geometry._
import scalafx.scene.paint.Color._

class World extends Canvas(800, 600) {
  drawTown(100, 200)
  drawTown(300, 300)
  drawRoute(100, 200, 300, 300)
  drawTrain(100, 200, 300, 300, 0.7)

  def drawTown(x: Double, y: Double): Unit = {
    graphicsContext2D.fill = Black
    graphicsContext2D.fillOval(x-15, y-15, 30, 30)
  }

  def drawRoute(x1: Double, y1: Double, x2: Double, y2: Double): Unit = {
    graphicsContext2D.stroke = Black
    graphicsContext2D.strokeLine(x1, y1, x2, y2)
  }

  def drawTrain(x1: Double, y1: Double, x2: Double, y2: Double, p: Double): Unit = {
    graphicsContext2D.fill = Red
    val x = x1 * (1-p) + x2 * p
    val y = y1 * (1-p) + y2 * p
    graphicsContext2D.fillOval(x-8, y-8, 16, 16)
  }
}
