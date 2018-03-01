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

/** Zoomable and dragable pane.
 */
class ZoomPane extends Pane {
  /** Minimum scale factor. */
  var minScale: Double = 0
  /** Maximum scale factor. */
  var maxScale: Double = Double.PositiveInfinity

  // On scroll, zoom around cursor
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

  // records cursor position when starting drag
  private var dragDelta: Point2D = new Point2D(0, 0)

  // start of drag : save mouse position
  onMousePressed = new EventHandler[MouseEvent] {
    override def handle(event: MouseEvent): Unit = {
      dragDelta = new Point2D(
        translateX() - event.getSceneX(),
        translateY() - event.getSceneY()
      )
    }
  }
  // dragging : update content position
  onMouseDragged = new EventHandler[MouseEvent] {
    override def handle(event: MouseEvent): Unit = {
      translateX = event.getSceneX() + dragDelta.x
      translateY = event.getSceneY() + dragDelta.y
    }
  }

  /** Zoom centered on a point.
   *
   *  @param factor the zoom factor.
   *  @param x the point x coordonate relative to the scene.
   *  @param y the point y coordonate relative to the scene.
   */
  def zoom(factor: Double, x: Double, y: Double): Unit = {
    var scale: Double = scaleX() * factor
    if (scale < minScale) scale = minScale
    if (scale > maxScale) scale = maxScale
    // correct to take clamping in account
    val correctFactor: Double = scale / scaleX()

    val bounds: Bounds = localToScene(boundsInLocal())
    // point position relative to node center
    val dx: Double = (x - (bounds.getWidth() / 2 + bounds.getMinX()))
    val dy: Double = (y - (bounds.getHeight() / 2 + bounds.getMinY()))

    // rescale and translate
    translateX = translateX() - (correctFactor - 1) * dx
    translateY = translateY() - (correctFactor - 1) * dy
    scaleX = scale
    scaleY = scale
  }
}
