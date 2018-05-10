package logic.travel

import logic.town._
import logic.game._
import logic.company._
import logic.vehicle._
import logic.game._
import logic.good._

import scalafx.collections._
import scalafx.beans.property._


/** Complex travel instructions.
 *
 *  Complex travels are done through a list of simple instructions:
 *  go to a town or wait.
 */
class Script(val company: Company, val vehicle: Vehicle) {
  sealed trait TravelInstruction {
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
      Game.delayAction(delay, () => onCompleted())
    }
  }

case class Buy(val good: Good, quantity: Double)
extends TravelInstruction {
  def execute(onCompleted: () => Unit, onFailed: String => Unit): Unit = {
      Game.delayAction(0, () => onCompleted())
    }
  }

case class Sell(val good: Good, quantity: Double)
extends TravelInstruction {
  def execute(onCompleted: () => Unit, onFailed: String => Unit): Unit = {
    Game.delayAction(0, () => onCompleted())
  }
}


  val instructions: ObservableBuffer[TravelInstruction] = ObservableBuffer()
  val repeat: BooleanProperty = BooleanProperty(false)
  val started: BooleanProperty = BooleanProperty(false)
  private var onTravel = false

  val ip: IntegerProperty = IntegerProperty(0)

  /** next travel step. */
  private def next(
    stepCompleted: () => Unit,
    travelCompleted: () => Unit,
    onFailed: String => Unit): Unit = {
    // reset when reaching the last instruction, if `repeat` is set
    if(ip() >= instructions.length && repeat()) ip() = 0

    if(ip() < instructions.length) {
      // next instruction
      instructions(ip()).execute(stepCompleted, onFailed)
    } else {
      // travel completed
      started() = false
      onTravel = false
      ip() = 0
      travelCompleted()
    }
  }

  /** perform travel recursively. */
  private def step(): Unit = {
    if(started()) {
      next(
        () => {ip() = ip() + 1; step()},
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
}
