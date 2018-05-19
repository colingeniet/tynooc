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

/** Information on any travel associated with a vehicle.
 */
class TravelInfo(vehicle: Vehicle) extends VBox(3) {
  private val vehicleInfo: VBox = VehicleUnitDetail(vehicle)
  private val company: Label = new Label {
    text <== vehicle.owner().name
    styleClass.remove("label")
    textFill = Colors(vehicle.owner())
  }

  private val dest: Label = new Label("destination :")
  private val destName: Label = new Label()
  private val destDist: Label = new Label()
  private val destETA: Label = new Label()

  private val next: Label = new Label("next stop :")
  private val nextName: Label = new Label()
  private val nextDist: Label = new Label()
  private val nextETA: Label = new Label()
  private val passengers: Label = new Label()

  private var contents: ScrollPane = new ScrollPane()

  private val arrivedLbl: Label = new Label()

  private val sep1: Separator = new Separator()
  private val sep2: Separator = new Separator()



  private def onNewTravel(travel: Travel): Unit = {
    destName.text = s" ${travel.destination.name}"
    destDist.text <== createStringBinding(
      () => f" dist : ${travel.totalRemainingDistance.toDouble}%.0f",
      travel.totalRemainingDistance)
    destETA.text <== createStringBinding(
      () => s" ETA : ${TimeFormatter.timeToHourString(travel.totalRemainingTime.toDouble)}",
      travel.totalRemainingTime)

    nextName.text <== createStringBinding(
      () => s" ${travel.nextTown().name}",
      travel.nextTown)
    nextDist.text <== createStringBinding(
      () => f" dist : ${travel.remainingDistance.toDouble}%.0f",
      travel.remainingDistance)
    nextETA.text <== createStringBinding(
      () => s" ETA : ${TimeFormatter.timeToHourString(travel.remainingTime.toDouble)}",
      travel.remainingTime)

    passengers.text <== createStringBinding(
      () => s"pass. : ${travel.passengerNumber.toInt}",
      travel.passengerNumber)

    contents = new ScrollPane {
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
  }

  private def onArrival(): Unit = {
    arrivedLbl.text = s"arrived at ${vehicle.town().name}"
    children = List(
      company,
      arrivedLbl,
      sep1,
      vehicleInfo)
  }

  private def onTravelChange(): Unit = {
    vehicle.travel() match {
      case None => onArrival()
      case Some(t) => onNewTravel(t)
    }
  }

  vehicle.travel.onChange(onTravelChange())
  onTravelChange()
}
