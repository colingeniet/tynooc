package logic.mission

import scalafx.Includes._
import scalafx.beans.property._
import scalafx.beans.binding._

import logic.town._
import logic.good._
import logic.vehicle._

import java.io._


abstract class Mission(
  val reward: Double,
  val from: Town,
  val to: Town,
  val time: Double)
extends Serializable {
}


/* The basic mission: you send X ressource to a city */
class HelpMission(reward: Double, from: Town, to: Town, time: Double, val good: Good, val quantity: Double)
extends Mission(reward, from, to, time) {
  @transient var done: DoubleProperty = DoubleProperty(0)

  def advance(q: Double): Double = {
    val q = quantity min (quantity - done())
    done() = done() + q
    q
  }

  @transient var completed: BooleanBinding = jfxBooleanBinding2sfx(done >= quantity)

  @throws(classOf[IOException])
  private def writeObject(stream: ObjectOutputStream): Unit = {
    stream.defaultWriteObject()
    stream.writeObject(this.done.toDouble)
  }

  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  private def readObject(stream: ObjectInputStream): Unit = {
    stream.defaultReadObject()
    this.done = DoubleProperty(stream.readObject().asInstanceOf[Double])
    this.completed = jfxBooleanBinding2sfx(done >= quantity)
  }
}
