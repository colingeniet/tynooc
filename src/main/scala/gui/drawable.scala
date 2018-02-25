package gui.draw

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.layout._

trait Drawable {
  /** Update content.
   *
   *  Implementation should be a fast method, updating what changes
   *  frequently. Avoid creating big objects.
   */
  def draw(): Unit = ()
}

class DrawableVBox extends VBox with Drawable
class DrawableHBox extends HBox with Drawable
