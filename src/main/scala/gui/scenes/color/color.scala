package gui.scenes.color

import scala.collection.mutable.HashMap
import scalafx.scene.paint.Color

import logic.company._

/** Player colors generator */
object Colors {
  private var level: Int = 0
  private var pos: Int = 0

  val colors: HashMap[Company, Color] = new HashMap[Company, Color] {
    override def default(company: Company): Color = {
      this(company) = Color.hsb(nextHue()*360, 1, 1)
      this(company)
    }
  }

  def init(company: Company): Unit = {
    level = 0
    pos = 0
    colors.clear()
    colors(company) = Color.hsb(nextHue()*360, 1, 1)
  }

  /** Get next color hue. */
  def nextHue(): Double = {
    // compute next hue between 0 and 1
    var hue: Double = 0.0
    if (level == 0) {
      // special initial comportment
      hue = pos.toDouble / 3
      pos += 1
      if (pos >= 3) {
        level = 3
        pos = 0
      }
    } else {
      hue = (pos + 0.5) / level
      pos += 1
      if (pos >= level) {
        level *= 2
        pos = 0
      }
    }
    hue
  }

  def apply(company: Company): Color = colors(company)

  def update(company: Company, color: Color): Unit = colors(company) = color
}
