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

class JFactory(
  @JacksonXmlProperty(localName = "type") val _type: String,
  @JacksonXmlProperty(localName = "size") val size: Int)

class JPort(
  @JacksonXmlProperty(localName = "beam_clearance") val beam_clearance: Int,
  @JacksonXmlProperty(localName = "size") val size: Int)

class JAirport(
  @JacksonXmlProperty(localName = "runway_length") val runway_length: Int,
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
  @JacksonXmlProperty(localName = "Port") val port: JPort)

class JRail(
  @JacksonXmlProperty(localName = "length") val length: Int,
  @JacksonXmlProperty(localName = "maximum_speed") val maximum_speed: Int,
  @JacksonXmlProperty(localName = "tracks") val tracks: Int,
  @JacksonXmlProperty(localName = "electrified") val electrified: String)

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
  @JacksonXmlProperty(localName = "Sea") val sea: String,
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

  def buildTown(c: JCity, minX: Int, minY: Int): Town = {
    var t = new Town(c.name, c.x - minX + 30, c.y - minY + 30, 1)
    t.addResidents(c.population / 3, Status.Well)
    t.addResidents(c.population / 3, Status.Poor)
    t.addResidents(c.population - t.population(), Status.Rich)
    t
  }

  def buildRoute(towns: List[Town], c: JConnection): Unit = {
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
      start.addRoute((new River(start, end, c.canal.length, c.canal.beam_clearance)))
      end.addRoute((new River(end, start, c.canal.length, c.canal.beam_clearance)))
    }
  }

  def convertToWorld(world_map: JMap): Try[World] = {
    val world: World = new World()
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
    val world_map: JMap = mapper.readValue(yaml, classOf[JMap])
    convertToWorld(world_map).get
  }
}
