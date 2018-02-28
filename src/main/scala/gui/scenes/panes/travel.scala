package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._
import scalafx.geometry._

import gui.draw._
import gui.scenes.elements._
import gui.scenes.panes._
import logic.train._
import logic.travel._


class TravelInfo(travel: Travel) extends DrawableVBox {
  private var trainInfo: TrainDetail = new TrainDetail(travel.train)
  private var lbl = new Label("travel info")

  children = List(lbl, trainInfo)
}
