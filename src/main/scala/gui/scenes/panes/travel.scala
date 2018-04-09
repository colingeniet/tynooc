package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.beans.binding._
import scalafx.scene.paint.Color

import gui.scenes.elements._
import gui.scenes.panes._
import gui.scenes.panes.vehicle._
import gui.scenes.color._
import formatter._
import logic.vehicle._
import logic.travel._
import logic.game._
import logic.good._

/** Information on a travel.
 */
class TravelInfo(travel: Travel) extends VBox {
  private val vehicleInfo: VBox = VehicleUnitDetail(travel.vehicle)

  private val dest: Label = new Label("destination :")
  private val destName: Label = new Label(s" ${travel.destination.name}")
  private val destDist: Label = new Label {
    text <== createStringBinding(
      () => f" dist : ${travel.totalRemainingDistance.toDouble}%.0f",
      travel.totalRemainingDistance)
  }
  private val destETA: Label = new Label {
    text <== createStringBinding(
      () => s" ETA : ${TimeFormatter.timeToHourString(travel.totalRemainingTime.toDouble)}",
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
      () => s" ETA : ${TimeFormatter.timeToHourString(travel.remainingTime.toDouble)}",
      travel.remainingTime)
  }
  private val passengers: Label = new Label {
    text <== createStringBinding(
      () => s"pass. : ${travel.passengerNumber.toInt}",
      travel.passengerNumber)
  }
  private val company: Label = new Label {
    text <== travel.company.name
    styleClass.remove("label")
    textFill = Colors(travel.company)
  }

  private val contents: ScrollPane = new ScrollPane {
    content = new VBox {
      children = Good.all.map(g => {
        new Label {
          text <== createStringBinding(
            () => f"${g.name}: ${travel.contents(g)()}%.1f",
            travel.contents(g))
        }
      })
    }
  }

  private val arrivedLbl: Label = new Label(s"arrived at ${travel.destination.name}")

  private val sep1: Separator = new Separator()
  private val sep2: Separator = new Separator()

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
    sep1,
    vehicleInfo,
    sep2,
    contents)
  spacing = 3

  private def onArrival(): Unit = {
    children = List(arrivedLbl, sep1, vehicleInfo)
  }

  travel.isDone.onChange(onArrival())
}
