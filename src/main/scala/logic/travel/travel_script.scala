package logic.travel

import logic.town._
import logic.game._
import logic.company._
import logic.vehicle._
import logic.game._

import scalafx.collections._
import scalafx.beans.property._

import java.io._


/** Complex travel instructions.
 *
 *  Complex travels are done through a list of simple instructions:
 *  go to a town or wait.
 */
@SerialVersionUID(0L)
class Script(val company: Company, val vehicle: Vehicle)
extends Serializable {
  @SerialVersionUID(0L)
  sealed trait TravelInstruction extends Serializable {
    def execute(onCompleted: () => Unit, onFailed: String => Unit): Unit
  }

  case class TravelTo(val town: Town)
  extends TravelInstruction {
    def execute(onCompleted: () => Unit, onFailed: String => Unit): Unit = {
      // class Travel does not work well with empty travels
      if(town == vehicle.town()) {
        // avoid infinite recursion by delaying of 0
        // this will delay until the next tick
        Game.delayAction(0, () => onCompleted())
      } else {
        company.launchTravel(vehicle, town, onCompleted)
      }
    }
  }

  case class Wait(val delay: Double)
  extends TravelInstruction {
    def execute(onCompleted: () => Unit, onFailed: String => Unit): Unit = {
      Game.delayAction(delay, onCompleted)
    }
  }


  @transient var instructions: ObservableBuffer[TravelInstruction] = ObservableBuffer()
  @transient var repeat: BooleanProperty = BooleanProperty(false)
  @transient var started: BooleanProperty = BooleanProperty(false)
  private var onTravel = false

  @transient var instrPointer: IntegerProperty = IntegerProperty(0)

  /** next travel step. */
  private def next(
    stepCompleted: () => Unit,
    travelCompleted: () => Unit,
    onFailed: String => Unit): Unit = {
    // reset when reaching the last instruction, if `repeat` is set
    if(instrPointer() >= instructions.length && repeat()) instrPointer() = 0

    if(instrPointer() < instructions.length) {
      // next instruction
      instructions(instrPointer()).execute(stepCompleted, onFailed)
    } else {
      // travel completed
      started() = false
      onTravel = false
      instrPointer() = 0
      travelCompleted()
    }
  }

  /** perform travel recursively. */
  private def step(): Unit = {
    if(started()) {
      next(
        () => {instrPointer() = instrPointer() + 1; step()},
        () => (),
        msg => {started() = false; Game.printMessage(msg)})
    } else {
      // `pause` flag has been set: stop
      onTravel = false
    }
  }

  /** Start the travel. */
  def start(): Unit = {
    if(!onTravel) {
      onTravel = true
      step()
    }
  }


  @throws(classOf[IOException])
  private def writeObject(stream: ObjectOutputStream): Unit = {
    stream.defaultWriteObject()
    stream.writeObject(this.instructions.toList)
    stream.writeObject(this.repeat())
    stream.writeObject(this.started())
    stream.writeObject(this.instrPointer.toInt)
  }

  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  private def readObject(stream: ObjectInputStream): Unit = {
    stream.defaultReadObject()
    this.instructions = ObservableBuffer[TravelInstruction](stream.readObject().asInstanceOf[List[TravelInstruction]])
    this.repeat = BooleanProperty(stream.readObject().asInstanceOf[Boolean])
    this.started = BooleanProperty(stream.readObject().asInstanceOf[Boolean])
    this.instrPointer = IntegerProperty(stream.readObject().asInstanceOf[Integer])
  }
}
