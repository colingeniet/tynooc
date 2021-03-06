package gui.scenes.elements

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._
import scalafx.beans.property._
import javafx.scene.input.ScrollEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.DragEvent
import javafx.event.EventHandler
import scalafx.geometry._

/** Zoomable and dragable pane.
 */
trait ZoomPane extends Node {
  val scale: DoubleProperty = DoubleProperty(1)

  /** Minimum scale factor. */
  var minScale: Double = 0
  /** Maximum scale factor. */
  var maxScale: Double = Double.PositiveInfinity

  def handleScroll(event: ScrollEvent): Unit = {
    var zoomFactor: Double = 1.15
    if (event.getDeltaY() <= 0) {
      // zoom out
      zoomFactor = 1 / zoomFactor
    }
    this.zoom(zoomFactor, event.getSceneX(), event.getSceneY())
    event.consume()
  }


  // records cursor position when starting drag
  private var dragDelta: Point2D = new Point2D(0, 0)

  // start of drag : save mouse position
  def handlePressed(event: MouseEvent): Unit = {
    dragDelta = new Point2D(
      translateX() - event.getSceneX(),
      translateY() - event.getSceneY())
  }

  // dragging : update content position
  def handleDragged(event: MouseEvent): Unit = {
    translateX = event.getSceneX() + dragDelta.x
    translateY = event.getSceneY() + dragDelta.y
  }


  /** Zoom centered on a point.
   *
   *  @param factor the zoom factor.
   *  @param x the point x coordonate relative to the scene.
   *  @param y the point y coordonate relative to the scene.
   */
   def zoom(factor: Double, x: Double, y: Double): Unit = {
     var newScale: Double = scale() * factor
     if (newScale < minScale) newScale = minScale
     if (newScale > maxScale) newScale = maxScale
     // correct to take clamping in account
     val correctFactor: Double = newScale / scale()

     val bounds: Bounds = localToScene(layoutBounds())
     // point position relative to node center
     val dx: Double = (x - bounds.getMinX())
     val dy: Double = (y - bounds.getMinY())

     // rescale and translate
     translateX = translateX() - (correctFactor - 1) * dx
     translateY = translateY() - (correctFactor - 1) * dy
     scale() = newScale
   }

  def focus(x: Double, y: Double): Unit = {
    translateX = -x
    translateY = -y
  }
}

class ZoomPaneContainer(_content: ZoomPane) extends Pane {
  children = _content

  onScroll = new EventHandler[ScrollEvent] {
    override def handle(event: ScrollEvent): Unit = {
      _content.handleScroll(event)
    }
  }

  onMousePressed = new EventHandler[MouseEvent] {
    override def handle(event: MouseEvent): Unit = {
      _content.handlePressed(event)
    }
  }

  onMouseDragged = new EventHandler[MouseEvent] {
    override def handle(event: MouseEvent): Unit = {
      _content.handleDragged(event)
    }
  }
}
