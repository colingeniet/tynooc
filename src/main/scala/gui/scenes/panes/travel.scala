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

  private var isArrived: Boolean = false
  private val arrivedLbl: Label = new Label()

  private val sep: Separator = new Separator()

  children = List(
    dest,
    destName,
    destDist,
    destETA,
    next,
    nextName,
    nextDist,
    nextETA,
    sep,
    trainInfo)
  spacing = 3

  override def draw(): Unit = {
    if(travel.isDone) {
      if(!isArrived) {
        arrivedLbl.text = "arrived at " + travel.destination.name
        children = List(arrivedLbl, sep, trainInfo)
        isArrived = true
      }
    } else {
      destName.text = " " + travel.destination.name
      destDist.text = f" dist : ${travel.totalRemainingDistance}%.0f"
      destETA.text = f" ETA : ${travel.totalRemainingTime}%.0f"
      nextName.text = " " + travel.nextTown.name
      nextDist.text = f" dist : ${travel.remainingDistance}%.0f"
      nextETA.text = f" ETA : ${travel.remainingTime}%.0f"
    }
  }
}
