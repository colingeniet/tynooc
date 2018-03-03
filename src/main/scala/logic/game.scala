package logic.game

import logic.player._
import logic.world._

object Game {
  private var last: Double = System.currentTimeMillis()
  var world: World = new World()
  var time: Double = 0

  var paused: Boolean = false
  var timeAcceleration: Double = 1

  def update(): Unit = {
    val a: Double = System.currentTimeMillis()
    if (!paused) {
      val dt: Double = timeAcceleration * realToVirtualTime(a - last)
      logic(dt)
      time += dt
    }
    last = a
  }

  def logic(dt: Double): Unit = {
    //Update Cities
    world.update(dt)
  }

  def reset(): Unit = {
    world = new World()
    time = 0
    paused = false
    timeAcceleration = 1
    last = System.currentTimeMillis()
  }

  // 4 sec (real time) = 1 hours (game time)
  def realToVirtualTime(t: Double): Double = t / 4000

  def timeToDateString(t: Double): String = ""
  def timeToHourString(t: Double): String =
    f"$t%.0fh${t*60%60}%.0f"
}
