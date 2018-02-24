package gui.scenes.elements

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._
import javafx.scene.input.ScrollEvent
import javafx.scene.input.MouseEvent
import javafx.event.EventHandler
import scalafx.geometry._

// I have no idea why it works with an AnchorPane
class ZoomPane extends AnchorPane {
  val minScale: Double = 0.1
  val maxScale: Double = 10

  onScroll = new EventHandler[ScrollEvent] {
    override def handle(event: ScrollEvent): Unit = {
      var zoomFactor: Double = 1.15
      if (event.getDeltaY() <= 0) {
        // zoom out
        zoomFactor = 1 / zoomFactor
      }
      zoom(zoomFactor, event.getSceneX(), event.getSceneY())
      event.consume()
    }
  }

  private var dragDelta: Point2D = new Point2D(0, 0)

  onMousePressed = new EventHandler[MouseEvent] {
    override def handle(event: MouseEvent): Unit = {
      // record a delta distance for drag
      dragDelta = new Point2D(
        translateX() - event.getSceneX(),
        translateY() - event.getSceneY()
      )
      cursor = Cursor.Move
    }
  }
  onMouseReleased = new EventHandler[MouseEvent] {
    override def handle(event: MouseEvent): Unit = {
      cursor = Cursor.Default
    }
  }
  onMouseDragged = new EventHandler[MouseEvent] {
    override def handle(event: MouseEvent): Unit = {
      translateX = event.getSceneX() + dragDelta.x
      translateY = event.getSceneY() + dragDelta.y
    }
  }

  /** Zoom centered on a point.
   *
   *  @param factor the zoom factor.
   *  @param x the point x coordonate.
   *  @param y the point y coordonate.
   */
  def zoom(factor: Double, x: Double, y: Double): Unit = {
    var scale: Double = scaleX() * factor
    if (scale < minScale) scale = minScale
    if (scale > maxScale) scale = maxScale
    // correct to take clamping in account
    var correctFactor: Double = scale / scaleX()

    var bounds: Bounds = localToScene(boundsInLocal())
    // point position relative to node center
    var dx: Double = (x - (bounds.getWidth() / 2 + bounds.getMinX()))
    var dy: Double = (y - (bounds.getHeight() / 2 + bounds.getMinY()))

    // rescale and translate
    translateX = translateX() - (correctFactor - 1) * dx
    translateY = translateY() - (correctFactor - 1) * dy
    scaleX = scale
    scaleY = scale
  }
}
