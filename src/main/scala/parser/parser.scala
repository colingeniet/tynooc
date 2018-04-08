package parser

import scala.io.Source
import collection.mutable.HashSet

import logic.town._
import logic.world._
import logic.route._
import logic.game._
import logic.facility._

import java.util.{List => JList}
import collection.JavaConverters._

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.core._

final case class BadFileFormatException(
  private val message: String = "",
  private val cause: Throwable = None.orNull)
extends Exception(message, cause)

class JFactory(
  @JacksonXmlProperty(localName = "type") val _type: String,
  @JacksonXmlProperty(localName = "size") val size: Int)

class JPort(
  @JacksonXmlProperty(localName = "beam_clearance") val beam_clearance: Int,
  @JacksonXmlProperty(localName = "size") val size: Int)

class JAirport(
  @JacksonXmlProperty(localName = "runway_length") val runway_length: Int,
  @JacksonXmlProperty(localName = "size") val size: Int)

class JTrainStation(
  @JacksonXmlProperty(localName = "size") val size: Int)

class JCity(
  @JacksonXmlProperty(localName = "name") val name: String,
  @JacksonXmlProperty(localName = "x") val x : Int,
  @JacksonXmlProperty(localName = "y") val y : Int,
  @JacksonXmlProperty(localName = "population") val population : Int,
  @JacksonXmlProperty(localName = "Factory")
  @JacksonXmlElementWrapper(useWrapping = false)
  val factories: JList[JFactory],
  @JacksonXmlProperty(localName = "Airport") val airport: JAirport,
  @JacksonXmlProperty(localName = "Port") val port: JPort,
  @JacksonXmlProperty(localName = "TrainStation") val trainStation: JTrainStation)

class JRail(
  @JacksonXmlProperty(localName = "length") val length: Int,
  @JacksonXmlProperty(localName = "maximum_speed") val maximum_speed: Int,
  @JacksonXmlProperty(localName = "tracks") val tracks: Int,
  @JacksonXmlProperty(localName = "electrified") val electrified: String)

class JSea

class JCanal(
  @JacksonXmlProperty(localName = "length") val length: Int,
  @JacksonXmlProperty(localName = "beam_clearance") val beam_clearance: Int)

class JRiver(
  @JacksonXmlProperty(localName = "length") val length: Int,
  @JacksonXmlProperty(localName = "beam_clearance") val beam_clearance: Int)

class JRoad(
  @JacksonXmlProperty(localName = "length") val length: Int,
  @JacksonXmlProperty(localName = "maximum_speed") val maximum_speed: Int,
  @JacksonXmlProperty(localName = "lanes") val lanes: Int)

class JConnection(
  @JacksonXmlProperty(localName = "Sea")
  @JacksonXmlElementWrapper(useWrapping = false)
  val sea: JList[JSea],
  @JacksonXmlProperty(localName = "River") val river: JRiver,
  @JacksonXmlProperty(localName = "Canal") val canal: JCanal,
  @JacksonXmlProperty(localName = "Road") val road: JRoad,
  @JacksonXmlProperty(localName = "Rail") val rail: JRail,
  @JacksonXmlProperty(localName = "upstream") val upstream: String,
  @JacksonXmlProperty(localName = "downstream") val downstream: String)

class JMap(
  @JacksonXmlProperty(localName = "name") val name: String,
  @JacksonXmlProperty(localName = "width") val width : Int,
  @JacksonXmlProperty(localName = "height") val height : Int,
  @JacksonXmlProperty(localName = "City")
  @JacksonXmlElementWrapper(useWrapping = false) val cities : JList[JCity],
  @JacksonXmlProperty(localName = "Connection")
  @JacksonXmlElementWrapper(useWrapping = false) val connections: JList[JConnection])

object Parser {

  def buildTown(c: JCity): Town = {
    val t = new Town(c.name, c.x, c.y, 1)
    Game.world.status.foreach { s => t.addResidents(c.population / 3, s) }
    t.addResidents(c.population - t.population(), Game.world.status.head)
    t
  }

  def buildFactory(town: Town, j: JFactory): Unit = {
    town.addFacility(new Factory(FactoryModel(j._type), town, Game.bigBrother))
  }

  def buildRoute(towns: HashSet[Town], c: JConnection): Unit = {
    val start: Town = towns.find(_.name == c.upstream).get
    val end: Town = towns.find(_.name == c.downstream).get
    if(c.road != null) {
      start.addRoute(new Road(start, end, c.road.length, c.road.maximum_speed,
                              c.road.lanes))
      end.addRoute(new Road(end, start, c.road.length, c.road.maximum_speed,
                             c.road.lanes))
    }
    if(c.rail != null) {
      val electrified = c.rail.electrified == "yes"
      start.addRoute((new Rail(start, end, c.rail.length, c.rail.maximum_speed,
                              c.rail.tracks, electrified)))
      end.addRoute((new Rail(end, start, c.rail.length, c.rail.maximum_speed,
                             c.rail.tracks, electrified)))
    }
    if(c.canal != null) {
      start.addRoute((new Canal(start, end, c.canal.length, c.canal.beam_clearance)))
      end.addRoute((new Canal(end, start, c.canal.length, c.canal.beam_clearance)))
    }
    if(c.river != null) {
      start.addRoute((new River(start, end, c.river.length, c.river.beam_clearance)))
      end.addRoute((new River(end, start, c.river.length, c.river.beam_clearance)))
    }
    if(c.sea != null) {
      val distX = start.x - end.x
      val distY = start.y - end.y
      val dist = math.hypot(distX, distY)
      start.addRoute((new Seaway(start, end, dist)))
      end.addRoute((new Seaway(end, start, dist)))
    }
  }

  def buildWorld(jMap: JMap): World = {
    val world: World = new World()
    if(jMap.cities == null)
      throw new BadFileFormatException("Invalid map file : no cities in the world.")
    if(jMap.connections == null)
      throw new BadFileFormatException("Invalid map file : no connections in the world.")

    jMap.cities.asScala.map { buildTown(_) }.foreach { world.addTown(_) }

    jMap.cities.asScala.foreach { c =>
      if(c.factories != null) {
        c.factories.asScala.foreach { f =>
          buildFactory(world.towns.find(_.name == c.name).get, f) }
      }
    }

    jMap.connections.asScala.filter { c =>
      c.upstream != null && c.downstream != null
    }.foreach { buildRoute(world.towns, _) }
    world
  }

  /** Parse a world file.
    *
    * @param filename The path of the file to parse.
    */
  def readWorldInformations(filename: String): World = {
    val file = Source.fromFile(filename)
    val yaml = file.getLines.mkString("\n")
    file.close()
    val mapper = new XmlMapper()
    val jMap = try {
      mapper.readValue(yaml, classOf[JMap])
    }
    catch {
      case e: JsonParseException => throw new BadFileFormatException("Invalid map file.")
    }
    buildWorld(jMap)
  }
}
