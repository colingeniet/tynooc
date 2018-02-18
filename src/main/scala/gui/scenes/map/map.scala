package gui.scenes.map

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.canvas._
import scalafx.event._
import scalafx.geometry._
import scalafx.scene.paint.Color._

import world.World

class Map(world: World) extends Canvas(800, 600) {
  world.towns.foreach {
    town => {
      drawTown(town.x, town.y)
      town.routes.foreach {
        route => drawRoute(
                   route.start.x,
                   route.start.y,
                   route.end.x,
                   route.end.y)
      }
    }
  }

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
