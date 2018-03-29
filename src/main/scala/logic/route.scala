package logic.route

import logic.town._

trait Connection {
  val start: Town
  val end: Town
  val length: Double
}

class Route(
  val start: Town,
  val end: Town,
  val length: Double)
extends Connection

class Rail(
  val start: Town ,
  val end: Town,
  val length: Double,
  val maximum_speed: Int,
  val tracks: Int,
  val electrified: Boolean)
extends Connection

class Canal(
  val start: Town,
  val end: Town,
  val length: Double,
  val beam_clearance: Int)
extends Connection

class River(
  val start: Town,
  val end: Town,
  val length: Double,
  val beam_clearance: Int)
extends Connection
