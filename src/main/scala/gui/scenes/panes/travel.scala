package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.beans.binding._

import gui.draw._
import gui.scenes.elements._
import gui.scenes.panes._
import logic.vehicle._
import logic.vehicle.train._
import logic.travel._
import logic.game._

/** Information on a travel.
 */
class TravelInfo(travel: Travel) extends DrawableVBox {
  private val trainInfo: TrainDetail = new TrainDetail(travel.train)

  private val dest: Label = new Label("destination :")
  private val destName: Label = new Label(travel.destination.name)
  private val destDist: Label = new Label {
    text <== createStringBinding(
      () => f" dist : ${travel.totalRemainingDistance.toDouble}%.0f",
      travel.totalRemainingDistance)
  }
  private val destETA: Label = new Label {
    text <== createStringBinding(
      () => s" ETA : ${Game.timeToHourString(travel.totalRemainingTime.toDouble)}",
      travel.totalRemainingTime)
  }

  private val next: Label = new Label("next stop :")
  private val nextName: Label = new Label {
    text <== createStringBinding(
      () => s" ${travel.nextTown().name}",
      travel.nextTown)
  }
  private val nextDist: Label = new Label {
    text <== createStringBinding(
      () => f" dist : ${travel.remainingDistance.toDouble}%.0f",
      travel.remainingDistance)
  }
  private val nextETA: Label = new Label {
    text <== createStringBinding(
      () => s" ETA : ${Game.timeToHourString(travel.remainingTime.toDouble)}",
      travel.remainingTime)
  }
  private val passengers: Label = new Label()
  private val company: Label = new Label(travel.company.name)

  private val arrivedLbl: Label = new Label(s"arrived at ${travel.destination.name}")

  private val sep: Separator = new Separator()

  children = List(
    company,
    dest,
    destName,
    destDist,
    destETA,
    next,
    nextName,
    nextDist,
    nextETA,
    passengers,
    sep,
    trainInfo)
  spacing = 3

  private def onArrival(): Unit = {
    children = List(arrivedLbl, sep, trainInfo)
  }

  travel.isDone.onChange(onArrival())

  override def draw(): Unit = {
    passengers.text = s"pass. : ${travel.passengerNumber}"
  }
}
