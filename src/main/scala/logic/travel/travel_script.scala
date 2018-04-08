package logic.travel

import logic.town._
import logic.game._
import logic.company._
import logic.vehicle._
import logic.game._

import scalafx.collections._
import scalafx.beans.property._


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
        try company.launchTravel(vehicle, town, onCompleted)
        catch {
          case IllegalActionException(msg, _) => onFailed(msg)
          case PathNotFoundException(msg, _) => onFailed(msg)
        }
      }
    }
  }

  case class Wait(val delay: Double)
  extends TravelInstruction {
    def execute(onCompleted: () => Unit, onFailed: String => Unit): Unit = {
      Game.delayAction(delay, onCompleted)
    }
  }


  val instructions: ObservableBuffer[TravelInstruction] = ObservableBuffer()
  val repeat: BooleanProperty = BooleanProperty(false)
  val paused: BooleanProperty = BooleanProperty(true)
  private var onTravel = false

  val ip: IntegerProperty = IntegerProperty(0)

  private def next(
    stepCompleted: () => Unit,
    travelCompleted: () => Unit,
    onFailed: String => Unit): Unit = {
    if(ip() >= instructions.length && repeat()) ip() = 0

    if(ip() < instructions.length) {
      instructions(ip()).execute(stepCompleted, onFailed)
    } else {
      paused() = true
      onTravel = false
      ip() = 0
      travelCompleted()
    }
  }

  private def step(): Unit = {
    if(!paused()) {
      next(
        () => {ip() = ip() + 1; step()},
        () => (),
        msg => {paused() = true; Game.printMessage(msg)})
    } else {
      onTravel = false
    }
  }

  def start(): Unit = {
    if(!onTravel) {
      onTravel = true
      step()
    }
  }
}
