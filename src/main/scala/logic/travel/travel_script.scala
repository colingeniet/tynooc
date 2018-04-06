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
      if(town == vehicle.town()) onCompleted()
      else {
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

  val ip: IntegerProperty = IntegerProperty(0)

  def next(
    stepCompleted: () => Unit,
    travelCompleted: () => Unit,
    onFailed: String => Unit): Unit = {
    if(ip() >= instructions.length && repeat()) ip() = 0

    if(ip() < instructions.length) {
      instructions(ip()).execute(stepCompleted, onFailed)
    } else {
      paused() = true
      ip() = 0
      travelCompleted()
    }
  }

  def step(): Unit = {
    if(!paused()) {
      next(
        () => {ip() = ip() + 1; step()},
        () => (),
        msg => {paused() = true; Game.printMessage(msg)})
    }
  }
}
