package logic.game

import scalafx.beans.property._

import scala.collection.mutable.PriorityQueue
import scala.math.Ordering.Implicits._
import java.io._

import logic.world._
import logic.company._
import ai._
import player._
import parser._

/** Game logic main object. */
object Game {
  /* Time of last update. Used to control simulation rate. */
  private var last: Double = System.currentTimeMillis()

  var world: World = new World()
  var time: DoubleProperty = DoubleProperty(0)
  private var nextDay: Double = 0
  /** List of the players */
  var players: List[Player] = List()
  /** Main player of the game. */
  var mainPlayer: Option[Player] = None
  /** Simulation rate control. */
  var paused: Boolean = false
  var timeAcceleration: Double = 1
  val economyTick: Double = 3
  /** Path of the map file. */
  var mapPath: File = new File("map/map.xml")

  var bigBrother: Company = new Company("Big Brother", null)

  var printMessage: String => Unit = (_ => ())


  private val queueOrdering = Ordering.by[(Double, () => Unit), Double](_._1).reverse

  private var actionQueue: PriorityQueue[(Double, () => Unit)] =
    new PriorityQueue[(Double, () => Unit)]()(queueOrdering)

  /** Execute an action at a given in game time. */
  def addAction(actionTime: Double, action: () => Unit) = {
    actionQueue.enqueue((actionTime, action))
  }

  def delayAction(delay: Double, action: () => Unit) = {
    addAction(time() + delay, action)
  }

  /** Advance simulation. */
  def update(): Unit = {
    val a: Double = System.currentTimeMillis()
    if (!paused) {
      // in game time passed
      val dt: Double = timeAcceleration * realToVirtualTime(a - last)
      time() = time() + dt

      if (time() >= nextDay) {
        world.update_towns()
        nextDay += economyTick
      }

      world.update(dt)
      players.foreach {
        case ai: AI => ai.play(world, dt)
        case _      =>
      }

      var actions: List[() => Unit] = List()
      while (!actionQueue.isEmpty && actionQueue.head._1 <= time()) {
        actions = actionQueue.dequeue()._2 :: actions
      }
      actions.foreach(_())
    }
    last = a
  }


  /** Init game state. */
  def init(): Unit = {
    bigBrother = new Company("Big Brother", null)
    world = Parser.readWorldInformations(mapPath)
    time() = 0
    nextDay = 0
    paused = false
    timeAcceleration = 1
    actionQueue.clear()
    last = System.currentTimeMillis()
  }

  // 2 sec (real time) = 1 hours (game time)
  val virtualToRealRatio: Double = 2000

  def realToVirtualTime(t: Double): Double = t / virtualToRealRatio
  def virtualToRealTime(t: Double): Double = t * virtualToRealRatio


  @SerialVersionUID(0L)
  private class GameData(
    val world: World,
    val time: Double,
    val nextDay: Double,
    val players: List[Player],
    val mainPlayer: Option[Player],
    val bigBrother: Company,
    val actionQueue: PriorityQueue[(Double, () => Unit)])
  extends Serializable

  private def game_data: GameData = {
    new GameData(
      world,
      time.toDouble,
      nextDay,
      players,
      mainPlayer,
      bigBrother,
      actionQueue)
  }

  private def game_data_=(data: GameData): Unit = {
    world = data.world
    time() = data.time
    last = System.currentTimeMillis()
    nextDay = data.nextDay
    players = data.players
    mainPlayer = data.mainPlayer
    bigBrother = data.bigBrother
    actionQueue = data.actionQueue
  }

  def save_game(stream: ObjectOutputStream): Unit = {
    stream.writeObject(game_data)
  }

  def load_game(stream: ObjectInputStream): Unit = {
    game_data = stream.readObject().asInstanceOf[GameData]
  }
}
