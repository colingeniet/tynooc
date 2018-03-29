package parser

import scala.util.{Try, Success, Failure}
import scala.io.Source

import logic.town._
import logic.world._
import logic.route._

import java.util.{List => JList}
import collection.JavaConverters._

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper

class Factory(
  @JacksonXmlProperty(localName = "type") val _type: String,
  @JacksonXmlProperty(localName = "size") val size: Int)

class Port(
  @JacksonXmlProperty(localName = "beam_clearance") val beam_clearance: Int,
  @JacksonXmlProperty(localName = "size") val size: Int)

class Airport(
  @JacksonXmlProperty(localName = "runway_length") val runway_length: Int,
  @JacksonXmlProperty(localName = "size") val size: Int)

class City(
  @JacksonXmlProperty(localName = "name") val name: String,
  @JacksonXmlProperty(localName = "x") val x : Int,
  @JacksonXmlProperty(localName = "y") val y : Int,
  @JacksonXmlProperty(localName = "population") val population : Int,
  @JacksonXmlProperty(localName = "Factory")
  @JacksonXmlElementWrapper(useWrapping = false)
  val factories: JList[Factory],
  @JacksonXmlProperty(localName = "Airport") val airport: Airport,
  @JacksonXmlProperty(localName = "Port") val port: Port)

class Rail(
  @JacksonXmlProperty(localName = "length") val length: Int,
  @JacksonXmlProperty(localName = "maximum_speed") val maximum_speed: Int,
  @JacksonXmlProperty(localName = "tracks") val tracks: Int,
  @JacksonXmlProperty(localName = "electrified") electrified: String)

class Canal(
  @JacksonXmlProperty(localName = "length") val length: Int,
  @JacksonXmlProperty(localName = "beam_clearance") val beam_clearance: Int)

class River(
  @JacksonXmlProperty(localName = "length") val length: Int,
  @JacksonXmlProperty(localName = "beam_clearance") val beam_clearance: Int)

class Road(
  @JacksonXmlProperty(localName = "length") val length: Int,
  @JacksonXmlProperty(localName = "maximum_speed") val maximum_speed: Int,
  @JacksonXmlProperty(localName = "lanes") val lanes: Int)

class Connection(
  @JacksonXmlProperty(localName = "Sea") val sea: String,
  @JacksonXmlProperty(localName = "River") val river: River,
  @JacksonXmlProperty(localName = "Canal") val canal: Canal,
  @JacksonXmlProperty(localName = "Road") val road: Road,
  @JacksonXmlProperty(localName = "Rail") val rail: Rail,
  @JacksonXmlProperty(localName = "upstream") val upstream: String,
  @JacksonXmlProperty(localName = "downstream") val downstream: String)

class Map(
  @JacksonXmlProperty(localName = "name") val name: String,
  @JacksonXmlProperty(localName = "width") val width : Int,
  @JacksonXmlProperty(localName = "height") val height : Int,
  @JacksonXmlProperty(localName = "City")
  @JacksonXmlElementWrapper(useWrapping = false) val cities : JList[City],
  @JacksonXmlProperty(localName = "Connection")
  @JacksonXmlElementWrapper(useWrapping = false) val connections: JList[Connection])

object Parser {
  def buildTown(c: City, minX: Int, minY: Int): Town = {
    var t = new Town(c.name, c.x - minX + 30, c.y - minY + 30, 1)
    t.addResidents(c.population / 3, Status.Well)
    t.addResidents(c.population / 3, Status.Poor)
    t.addResidents(c.population - t.population(), Status.Rich)
    t
  }

  //def buildRoute(start: Town, end: Town, road: Road): Unit = {
  def buildRoute(start: Town, end: Town, length: Double): Unit = {
    start.addRoute(end, length)
    end.addRoute(start, length)
  }

  /* Code to optimize. Note that it adds town1 -> town2 and town1 -> town2. */
  def buildRoute(towns: List[Town], c: Connection): Unit = {
    val start: Town = towns.find(_.name == c.upstream).get
    val end: Town = towns.find(_.name == c.downstream).get
    if(c.rail != null) { buildRoute(start, end, c.rail.length) }
    if(c.road != null) { buildRoute(start, end, c.road.length) }
    if(c.canal != null) { buildRoute(start, end, c.canal.length) }
    if(c.river != null) { buildRoute(start, end, c.river.length) }
  }

  def convertToWorld(world_map: Map): Try[World] = {
    val world: World = new World(world_map.width, world_map.height)
    val minX: Int = world_map.cities.asScala.minBy {_.x }.x
    val minY: Int = world_map.cities.asScala.minBy {_.y }.y
    Try {
      if(world_map.cities == null)
        throw new IllegalArgumentException("No cities in the world.")
      if(world_map.connections == null)
        throw new IllegalArgumentException("No connections in the world.")

      val towns = world_map.cities.asScala.toList.map {
        c => buildTown(c, minX, minY)
      }
      towns.foreach { t => world.addTown(t) }

      world_map.connections.asScala.toList.filter { c =>
        c.upstream != null && c.downstream != null
      }.foreach { c => buildRoute(towns.toList, c) }

      world
    }
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
    val world_map: Map = mapper.readValue(yaml, classOf[Map])
    convertToWorld(world_map).get
  }
}
