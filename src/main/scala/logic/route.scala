package logic.route

import logic.town._

import logic.vehicle._

/** Generic route trait. */
trait Route {
  val start: Town
  val end: Town
  val length: Double

  /** Tests if a vehicle can use the route. */
  def accepts(vehicle: Vehicle): Boolean

  /** Route type name */
  def name: String = this.getClass().getSimpleName().toLowerCase()
}

/* Route types */

class Road(
  val start: Town,
  val end: Town,
  val length: Double,
  val maximum_speed: Double,
  val lanes: Int)
extends Route {
  def accepts(vehicle: Vehicle): Boolean = {
    vehicle match {
      case _: Truck => true
      case _ => false
    }
  }
}

class Airway(
  val start: Town,
  val end: Town,
  val length: Double)
extends Route {
  def accepts(vehicle: Vehicle): Boolean = {
    vehicle match {
      case _: Plane => true
      case _ => false
    }
  }
}

class Seaway(
  val start: Town,
  val end: Town,
  val length: Double)
extends Route {
  def accepts(vehicle: Vehicle): Boolean = {
    vehicle match {
      case _: Ship => true
      case _ => false
    }
  }
}

class Rail(
  val start: Town,
  val end: Town,
  val length: Double,
  val maximum_speed: Double,
  val tracks: Int,
  val electrified: Boolean)
extends Route {
  def accepts(vehicle: Vehicle): Boolean = {
    vehicle match {
      case _: Engine => true
      case _ => false
    }
  }
}

class Canal(
  val start: Town,
  val end: Town,
  val length: Double,
  val beam_clearance: Int)
extends Route {
  def accepts(vehicle: Vehicle): Boolean = {
    vehicle match {
      case s: Ship => s.model.beamClearance <= beam_clearance
      case _ => false
    }
  }
}

class River(
  val start: Town,
  val end: Town,
  val length: Double,
  val beam_clearance: Int)
extends Route {
  def accepts(vehicle: Vehicle): Boolean = {
    vehicle match {
      case s: Ship => s.model.beamClearance <= beam_clearance
      case _ => false
    }
  }
}
