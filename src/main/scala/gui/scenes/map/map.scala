package gui.scenes.map

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.shape._
import scalafx.scene.layout._
import scalafx.event._
import scalafx.scene.paint.Color._

import world.World

class Map(world: World) extends Pane {
  world.towns.foreach {
    town => {
      addTown(town)
      town.routes.foreach{ route => addRoute(route) }
    }
  }

  def addTown(town: World.Town): Unit = {
    var point: Circle = new Circle()
    point.centerX = town.x
    point.centerY = town.y
    point.radius = 15
    point.fill = Black
    children.add(point)
  }

  def addRoute(route: World.Route): Unit = {
    var line: Line = new Line()
    line.startX = route.start.x
    line.startY = route.start.y
    line.endX = route.end.x
    line.endY = route.end.y
    line.stroke = Black
    children.add(line)
  }

  def drawTrain(x1: Double, y1: Double, x2: Double, y2: Double, p: Double): Unit = {
    val x = x1 * (1-p) + x2 * p
    val y = y1 * (1-p) + y2 * p
    var point: Circle = new Circle()
    point.centerX = x
    point.centerY = y
    point.radius = 6
    point.fill = Red
    children.add(point)
  }
}
