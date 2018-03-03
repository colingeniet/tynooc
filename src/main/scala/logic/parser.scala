package logic.parser

import scala.io.Source
import logic.town._
import logic.world._
import logic.route._

object Parser {
  def parseTown(line: String, world: World): Town = {
    val infos = line.split(", ")
    if(infos.length < 7) // BAD
      println("Error bad file.")
    val (name, x, y, w, p1, p2, p3) = (infos(0), infos(1).toDouble, infos(2).toDouble, 
                                       infos(3).toDouble, infos(4).toInt, 
                                       infos(5).toInt, infos(6).toInt)    
    val town = new Town(name, x, y, w)
    town.addResidents(p1, world.status(0))
    town.addResidents(p2, world.status(1))
    town.addResidents(p3, world.status(2))
    town
  }
  
  def parseRoute(line: String, towns: List[Town]): Route = {
    val infos = line.split(" ")
    if(infos.length < 4) //BAD
      println("Error bad file.")
    val (id0, id1, length, damage) = (infos(0).toInt, infos(1).toInt, 
                                      infos(2).toDouble, infos(3).toDouble)
    println(s"$id0, $id1 ${towns(id0).name}")
    val (start, end) = (towns(id0), towns(id1))
    new Route(start, end, length, damage)
  }
  
  def readWorldInformations(filename: String): World = {
    val world = new World()
    var towns: List[Town] = List()
    var lines = Source.fromFile(filename).getLines.toList
    while(!lines.isEmpty && lines.head != "#") {
      var town = parseTown(lines.head, world)
      towns = town :: towns
      world.addTown(town)
      lines = lines.tail
    }
    towns = towns.reverse 
    if(!lines.isEmpty)
      lines = lines.tail
    while(!lines.isEmpty) {
      var route = parseRoute(lines.head, towns)
      route.start.addRoute(route)
      lines = lines.tail
    }
    world
  }
}