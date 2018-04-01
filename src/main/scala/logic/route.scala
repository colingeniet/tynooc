package logic.route

import logic.town._

trait Route {
  val start: Town
  val end: Town
  val length: Double
}

class Road(
  val start: Town,
  val end: Town,
  val length: Double,
  val maximum_speed: Int,
  val lanes: Int)
extends Route

class Airway(
  val start: Town,
  val end: Town,
  val length: Double)
extends Route

class Seaway(
  val start: Town,
  val end: Town,
  val length: Double)
extends Route

class Rail(
  val start: Town,
  val end: Town,
  val length: Double,
  val maximum_speed: Int,
  val tracks: Int,
  val electrified: Boolean)
extends Route

class Canal(
  val start: Town,
  val end: Town,
  val length: Double,
  val beam_clearance: Int)
extends Route

class River(
  val start: Town,
  val end: Town,
  val length: Double,
  val beam_clearance: Int)
extends Route
