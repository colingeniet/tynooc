package parser

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
  @JacksonXmlProperty(localName = "size") val size: Int) {

  override def toString: String = {
    s"${_type} factory of size ${size}."
  }
}

class Port(
  @JacksonXmlProperty(localName = "beam_clearance") val beam_clearance: Int,
  @JacksonXmlProperty(localName = "size") val size: Int) {

  override def toString: String = {
    s"Port of size ${size} and beam clearance ${beam_clearance}."
  }
}

class Airport(
  @JacksonXmlProperty(localName = "runway_length") val runway_length: Int,
  @JacksonXmlProperty(localName = "size") val size: Int) {

  override def toString: String = {
    s"Airport of size ${size} and runway length ${runway_length}."
  }
}

class City(
  @JacksonXmlProperty(localName = "name") val name: String,
  @JacksonXmlProperty(localName = "x") val x : Int,
  @JacksonXmlProperty(localName = "y") val y : Int,
  @JacksonXmlProperty(localName = "population") val population : Int,
  @JacksonXmlProperty(localName = "Factory")
  @JacksonXmlElementWrapper(useWrapping = false)
  _factories: JList[Factory],
  @JacksonXmlProperty(localName = "Airport") val airport: Airport,
  @JacksonXmlProperty(localName = "Port") val port: Port) {
  var factories: List[Factory] = List()
  if(_factories != null) {
    _factories.asScala.foreach (f => factories = f::factories)
  }

  override def toString: String = {
    s"${name}, ${x} ${y}, ${population}\n" +
    factories.foldLeft("") { (a, f) => a + s"  ${f.toString}\n" }
  }
}

class Rail(
  @JacksonXmlProperty(localName = "length") val length: Int,
  @JacksonXmlProperty(localName = "maximum_speed") val maximum_speed: Int,
  @JacksonXmlProperty(localName = "tracks") val tracks: Int,
  @JacksonXmlProperty(localName = "electrified") _electrified: String){
  val electrified = _electrified == "yes"
}

class Canal(
  @JacksonXmlProperty(localName = "length") val length: Int,
  @JacksonXmlProperty(localName = "beam_clearance") val beam_clearance: Int){
}

class River(
  @JacksonXmlProperty(localName = "length") val length: Int,
  @JacksonXmlProperty(localName = "beam_clearance") val beam_clearance: Int){
}

class Road(
  @JacksonXmlProperty(localName = "length") val length: Int,
  @JacksonXmlProperty(localName = "maximum_speed") val maximum_speed: Int,
  @JacksonXmlProperty(localName = "lanes") val lanes: Int){
}

class Connection(
  @JacksonXmlProperty(localName = "Sea") val sea: String,
  @JacksonXmlProperty(localName = "River") val river: River,
  @JacksonXmlProperty(localName = "Canal") val canal: Canal,
  @JacksonXmlProperty(localName = "Road") val road: Road,
  @JacksonXmlProperty(localName = "Rail") val rail: Rail,
  @JacksonXmlProperty(localName = "upstream") val upstream: String,
  @JacksonXmlProperty(localName = "downstream") val downstream: String) {

  override def toString: String = {
    var ret = s"connection from ${upstream} to ${downstream}\n"
    if(sea != null) { ret += "  sea\n" }
    if(river != null) { ret += "  river\n" }
    if(canal != null) { ret += "  canal\n" }
    if(road != null) { ret += "  road\n" }
    if(rail != null) { ret += "  rail\n" }
    ret
  }
}

class Map(
  @JacksonXmlProperty(localName = "name") val name: String,
  @JacksonXmlProperty(localName = "width") val width : Int,
  @JacksonXmlProperty(localName = "height") val height : Int,
  @JacksonXmlProperty(localName = "City")
  @JacksonXmlElementWrapper(useWrapping = false)
  _city : JList[City],
  @JacksonXmlProperty(localName = "Connection")
  @JacksonXmlElementWrapper(useWrapping = false)
  _connections: JList[Connection]) {

  var city: List[City] = List()
  var connections: List[Connection] = List()
  if(_city != null) {
    _city.asScala.foreach { c => city = c::city }
  }
  if(_connections != null) {
    _connections.asScala.foreach { c => connections = c::connections}
  }

  override def toString: String = {
    var str = s"${name} of width ${width} and height ${height}"
    str += city.foldLeft("") { (a, c) => a + s" ${c.toString}\n" }
    str += connections.foldLeft("") { (a, c) => a + s" ${c.toString}\n" }
    str
  }

}

object Parser {
  def buildTown(c: City, minX: Int, minY: Int): Town = {
    var t = new Town(c.name, c.x - minX + 30, c.y - minY + 30, 1)
    t.addResidents(c.population / 3, Status.Well)
    t.addResidents(c.population / 3, Status.Poor)
    t.addResidents(c.population - t.population(), Status.Rich)
    t
  }

  /* Code to optimize. Note that it adds town1 -> town2 and town1 -> town2. */
  def buildRoute(towns: List[Town], connection: Connection): Unit = {
    if(connection.rail != null) {
      val rail = connection.rail
      val town1: Town = towns.find(_.name == connection.upstream).getOrElse(towns(0))
      val town2: Town = towns.find(_.name == connection.downstream).getOrElse(towns(1))
      town1.addRoute(new Route(town1, town2, rail.length))
      town2.addRoute(new Route(town2, town1, rail.length))
    }
    if(connection.road != null) {
      val rail = connection.road
      val town1: Town = towns.find(_.name == connection.upstream).getOrElse(towns(0))
      val town2: Town = towns.find(_.name == connection.downstream).getOrElse(towns(1))
      town1.addRoute(new Route(town1, town2, rail.length))
      town2.addRoute(new Route(town2, town1, rail.length))
    }
    if(connection.canal != null) {
      val rail = connection.canal
      val town1: Town = towns.find(_.name == connection.upstream).getOrElse(towns(0))
      val town2: Town = towns.find(_.name == connection.downstream).getOrElse(towns(1))
      town1.addRoute(new Route(town1, town2, rail.length))
      town2.addRoute(new Route(town2, town1, rail.length))
    }
    if(connection.river != null) {
      val rail = connection.river
      val town1: Town = towns.find(_.name == connection.upstream).getOrElse(towns(0))
      val town2: Town = towns.find(_.name == connection.downstream).getOrElse(towns(1))
      town1.addRoute(new Route(town1, town2, rail.length))
      town2.addRoute(new Route(town2, town1, rail.length))
    }
  }

  def convertToWorld(world_map: Map): World = {
    val world: World = new World(world_map.width, world_map.height)
    val minX: Int = world_map.city.minBy {_.x }.x
    val minY: Int = world_map.city.minBy {_.y }.y
    println(minX, minY)
    var towns: List[Town] = world_map.city.map { c => buildTown(c, minX, minY) }
    world_map.connections.foreach { c => buildRoute(towns, c) }
    towns.foreach { t => world.addTown(t) }
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
    val world_map: Map = mapper.readValue(yaml, classOf[Map])
    convertToWorld(world_map)
  }
}
