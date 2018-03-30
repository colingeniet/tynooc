package logic.game

import scalafx.beans.property._

import scala.collection.mutable.PriorityQueue

import logic.world._
import ai._
import player._
import parser._

/** Game logic main object. */
object Game {
  /* Time of last update. Used to control simulation rate. */
  private var last: Double = System.currentTimeMillis()

  var world: World = new World()
  var time: DoubleProperty = DoubleProperty(0)
  /** List of the players */
  var players: List[Player] = List()
  /** Main player of the game. */
  var mainPlayer: Option[Player] = None
  /** Simulation rate control. */
  var paused: Boolean = false
  var timeAcceleration: Double = 1
  /** Path of the map file. */
  var mapPath = "map/cachan.xml"

  private var actionQueue: PriorityQueue[(Double, () => Unit)] =
    new PriorityQueue[(Double, () => Unit)]()(Ordering.by(_._1))

  /** Execute an action at a given in game time. */
  def addAction(actionTime: Double, action: () => Unit) =
    actionQueue.enqueue((actionTime, action))

  def delayAction(delay: Double, action: () => Unit) =
    addAction(time() + delay, action)

  /** Advance simulation. */
  def update(): Unit = {
    val a: Double = System.currentTimeMillis()
    if (!paused) {
      // in game time passed
      val dt: Double = timeAcceleration * realToVirtualTime(a - last)
      time() = time() + dt

      logic(dt)
      players.foreach {
        case ai: AI => ai.play(world, dt)
        case _      =>
      }
      while (!actionQueue.isEmpty && actionQueue.head._1 <= time()) {
        actionQueue.dequeue()._2()
      }
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
    time() = 0
    paused = false
    timeAcceleration = 1
    actionQueue = new PriorityQueue[(Double, () => Unit)]()(Ordering.by(_._1))
    last = System.currentTimeMillis()
  }

  val virtualToRealRatio: Double = 4000

  // 4 sec (real time) = 1 hours (game time)
  def realToVirtualTime(t: Double): Double = t / virtualToRealRatio
  def virtualToRealTime(t: Double): Double = t * virtualToRealRatio

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
