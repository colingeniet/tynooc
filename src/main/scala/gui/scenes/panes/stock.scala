package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._
import scalafx.scene.input.MouseEvent

import gui.scenes.elements._
import gui.scenes.panes.vehicle._
import logic.company._
import logic.vehicle._
import logic.world._
import logic.town._
import formatter._

import scala.util.Try

/** Displays a company rolling stock.
 *
 *  @param company the company.
 *  @param statsTrain a callback used to display information on a train.
 *  @param statsEngine a callback used to display information on a engine.
 *  @param statsCarriage a callback used to display information on a carriage.
 */
class VehicleList(
  company: Company,
  stats: VehicleUnit => Unit)
extends VBox(3) {
  private var list: Node = new SelectionList[VehicleUnit](
    company.vehicleUnits,
    _.model.name,
    detailVehicle(_))

  private val sep: Separator = new Separator()

  children = List(list)


  /** Displays a specific engine. */
  private def detailVehicle(vehicle: VehicleUnit): Unit = {
    // display stats in a separate window via callback
    stats(vehicle)

    children = List(list, sep, VehicleUnitMenu(vehicle))
  }
}
