package gui.scenes.map

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.shape._
import scalafx.scene.text._
import scalafx.scene.layout._
import scalafx.event._
import javafx.scene.input.MouseEvent
import javafx.event.EventHandler
import scalafx.scene.paint.Color._

import gui.scenes.elements._
import logic.world.World

class Map(
  world: World,
  displayTown: World.Town => Unit,
  displayRoute: World.Route => Unit)
extends ZoomPane {
  world.towns.foreach {
    town => {
      addTown(town)
      town.routes.foreach{ route => addRoute(route) }
    }
  }

  styleClass.add("map")

  minScale = 0.2
  maxScale = 4

  def addTown(town: World.Town): Unit = {
    var point: Circle = new Circle()
    point.centerX = town.x
    point.centerY = town.y
    point.radius = 12
    point.fill = Black
    point.onMouseClicked = new EventHandler[MouseEvent] {
      override def handle(event: MouseEvent) {
        displayTown(town)
      }
    }
    children.add(point)

    var text: Text = new Text(town.x + 9, town.y - 9, town.name) {
      mouseTransparent = true
      styleClass.add("town-name")
    }
    children.add(text)
  }

  def addRoute(route: World.Route): Unit = {
    var line: Line = new Line()
    line.startX = route.start.x
    line.startY = route.start.y
    line.endX = route.end.x
    line.endY = route.end.y
    line.stroke = Black
    line.strokeWidth = 5
    line.onMouseClicked = new EventHandler[MouseEvent] {
      override def handle(event: MouseEvent) {
        displayRoute(route)
      }
    }
    children.add(line)
  }

  def drawTrain(x1: Double, y1: Double, x2: Double, y2: Double, p: Double): Unit = {
    val x = x1 * (1-p) + x2 * p
    val y = y1 * (1-p) + y2 * p
    var point: Circle = new Circle()
    point.centerX = x
    point.centerY = y
    point.radius = 8
    point.fill = Red
    children.add(point)
  }
}
