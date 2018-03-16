package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._

import gui.draw._
import gui.scenes.elements._
import gui.scenes.panes._
import logic.train._
import logic.travel._
import logic.game._

/** Information on a travel.
 */
class TravelInfo(travel: Travel) extends DrawableVBox {
  private val trainInfo: TrainDetail = new TrainDetail(travel.train)
  private val dest: Label = new Label("destination :")
  private val destName: Label = new Label()
  private val destDist: Label = new Label()
  private val destETA: Label = new Label()
  private val next: Label = new Label("next stop :")
  private val nextName: Label = new Label()
  private val nextDist: Label = new Label()
  private val nextETA: Label = new Label()
  private val passengers: Label = new Label()
  private val company: Label = new Label()

  private var isArrived: Boolean = false
  private val arrivedLbl: Label = new Label()

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

  override def draw(): Unit = {
    if(travel.isDone()) {
      // only needs to update on arrival
      if(!isArrived) {
        arrivedLbl.text = s"arrived at ${travel.destination.name}"
        children = List(arrivedLbl, sep, trainInfo)
        isArrived = true
      }
    } else {
      destName.text = s" ${travel.destination.name}"
      destDist.text = f" dist : ${travel.totalRemainingDistance.toDouble}%.0f"
      destETA.text = s" ETA : ${Game.timeToHourString(travel.totalRemainingTime.toDouble)}"
      nextName.text = s" ${travel.nextTown().name}"
      nextDist.text = f" dist : ${travel.remainingDistance.toDouble}%.0f"
      nextETA.text = s" ETA : ${Game.timeToHourString(travel.remainingTime.toDouble)}"
      passengers.text = s"pass. : ${travel.passengerNumber}"
    }
    company.text = travel.company.name
    trainInfo.draw()
  }
}
