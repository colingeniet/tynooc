package logic.game

import logic.world._
import ai._
import player._
import parser._

/** Game logic main object. */
object Game {
  /* Time of last update. Used to control simulation rate. */
  private var last: Double = System.currentTimeMillis()

  var world: World = null
  var time: Double = 0
  /** List of the players */
  var players: List[Player] = List()
  /** Main player of the game. */
  var mainPlayer: Option[Player] = None
  /** Simulation rate control. */
  var paused: Boolean = false
  var timeAcceleration: Double = 1
  /** Path of the map file. */
  var mapPath = "map/Map"

  /** Advance simulation. */
  def update(): Unit = {
    val a: Double = System.currentTimeMillis()
    if (!paused) {
      // in game time passed
      val dt: Double = timeAcceleration * realToVirtualTime(a - last)
      logic(dt)
      players.foreach {
        case ai: AI => ai.play(world, dt)
        case _      =>
      }
      time += dt
    }
    last = a
  }

  /** Game logic simulation step.
    *
    * @param dt in game time passed since last step.
    */
  def logic(dt: Double): Unit = {
    //Update Cities
    world.update(dt)
  }

  /** Init game state. */
  def init(): Unit = {
    world = Parser.readWorldInformations(mapPath)
    time = 0
    paused = false
    timeAcceleration = 1
    last = System.currentTimeMillis()
  }

  // 4 sec (real time) = 1 hours (game time)
  def realToVirtualTime(t: Double): Double = t / 4000

  /** Convert a time as a double to its string representation.
    *
    * Format : <Hours>h<Min>
    *
    *  @param t The <code>Double</code> value (is interpreted as hours).
    */
  def timeToHourString(t: Double): String =
    f"${t.floor}%02.0fh${t * 60 % 60}%02.0f"

  /** Convert a date as a double to its string representation.
    *
    *  Days numbering start from 1.
    *  Format : <Day> : <Hours>h<Min>
    *
    * @param t the <code>Doble</code> value (is interpreted as hours)
    */
  def timeToDateString(t: Double): String =
    f"day ${(t.toInt / 24 + 1)}%d : " + timeToHourString(t % 24)
}
