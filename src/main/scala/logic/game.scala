package logic.game

import logic.player._
import logic.world._
import ia._

/** Game logic main object. */
object Game {
  /* Time of last update. Used to control simulation rate. */
  private var last: Double = System.currentTimeMillis()

  var world: World = new World()
  var time: Double = 0
  var ia: IA = null  
    
  /* Simulation rate control. */
  var paused: Boolean = false
  var timeAcceleration: Double = 1

  /** Advance simulation.
   */
  def update(iap: Option[IA]): Unit = {
    val a: Double = System.currentTimeMillis()
    if (!paused) {
      // in game time passed
      val dt: Double = timeAcceleration * realToVirtualTime(a - last)
      logic(dt)
      if(iap.isDefined)
        iap.get.play(dt)
      ia.play(dt)
      time += dt
    }
    last = a
  }

  /** Game logic simulation step.
   *
   *  @param dt in game time passed since last step.
   */
  def logic(dt: Double): Unit = {
    //Update Cities
    world.update(dt)
  }

  /** Reinitialize game state. */
  def reset(): Unit = {
    world = new World()
    time = 0
    paused = false
    timeAcceleration = 1
    last = System.currentTimeMillis()
  }

  // 4 sec (real time) = 1 hours (game time)
  def realToVirtualTime(t: Double): Double = t / 4000

  /** Convert a time as a double to its string representation.
   *
   *  The double value is interpreted as hours.
   *  Format : <Hours>h<Min>
   */
  def timeToHourString(t: Double): String =
    f"${t.floor}%02.0fh${t * 60 % 60}%02.0f"

  /** Convert a date as a double to its string representation.
   *
   *  The double value is interpreted as hours.
   *  Days numbering start from 1.
   *  Format : <Day> : <Hours>h<Min>
   */
  def timeToDateString(t: Double): String =
    f"day ${(t.toInt / 24 + 1)}%d : " + timeToHourString(t % 24)
}
