package logic.world

import logic.graph._
import logic.town._
import logic.travel._

object Status {
  var _id = 0

  sealed class Val(val id: Int) {
    _id += 1
    def this() { this(_id) }
  }

  object RICH extends Status.Val
  object POOR extends Status.Val
  object WELL extends Status.Val
}

class World extends Graph {
  var status = List(Status.RICH, Status.POOR, Status.WELL)
  var statusNumber = 3
  var townNumber = 0

  private var _towns: List[Town] = List()
  private var _travels: List[Travel] = List()

  def towns: List[Town] = _towns
  def vertices: List[Town] = towns
  def travels: List[Travel] = _travels
  def population: Int = towns.foldLeft[Int](0) { _ + _.population }

  def addTown(town: Town): Unit = { _towns = town :: _towns; townNumber += 1}

  def addTown(name: String, x: Double, y: Double, welcomingLevel: Double): Unit = {
    _towns = new Town(name, x, y, welcomingLevel) :: _towns
    townNumber += 1
  }

  def tryTravel(start:Town, destination:Town, migrantByStatus:Array[Int]):Unit = {}

  def update(dt: Double): Unit = { }

  override def toString: String = {
    towns.foldLeft[String]("") { (d, t) => d + s"$t\n" }
  }
}

object World {
  def realToVirtualTime(t: Double) : Double = 50*t
  def virtualToRealTime(t: Double) : Double = t/50
}
