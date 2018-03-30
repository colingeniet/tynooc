package logic.travel

import logic.town._
import logic.game._
import logic.company._
import logic.vehicle._


trait TravelInstruction {
  def execute(onCompleted: () => Unit = () => ()): Unit
}

case class TravelTo(company: Company, train: Engine, town: Town)
extends TravelInstruction {
  def execute(onCompleted: () => Unit): Unit = {
    company.launchTravel(train, town, onCompleted)
  }
}

case class Wait(delay: Double)
extends TravelInstruction {
  def execute(onCompleted: () => Unit): Unit = {
    Game.delayAction(delay, onCompleted)
  }
}

case class DoTimes(times: Int, instr: List[TravelInstruction])
extends TravelInstruction {
  def execute(onCompleted: () => Unit): Unit = executePartial(onCompleted)

  private def executePartial(
    onCompleted: () => Unit,
    timesLeft: Int = times,
    instrLeft: List[TravelInstruction] = instr): Unit = {
    if(instrLeft.isEmpty) {
      if(timesLeft > 0) executePartial(onCompleted, timesLeft-1)
      else onCompleted()
    } else {
      instrLeft.head.execute(() => executePartial(onCompleted, timesLeft, instrLeft.tail))
    }
  }
}
