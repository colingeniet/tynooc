package parser

import scala.io.Source
import logic.town._
import logic.world._
import logic.route._

object Parser {
  private def parseTown(line: String, world: World): Town = {
    val infos = line.split(", ")
    if(infos.length < 4 + world.statusNumber) // BAD
      println("Error bad file.")
    val (name, x, y, w) = (infos(0), infos(1).toDouble, infos(2).toDouble,
                           infos(3).toDouble)
    val p = infos.slice(4, world.statusNumber + 5)
    val town = new Town(name, x, y, w)
    for(i <- 0 to world.statusNumber - 1) {
      town.addResidents(p(i).toInt, world.status(0))
    }
    town
  }

  private def parseRoute(line: String, towns: List[Town]): Route = {
    val infos = line.split(", ")
    if(infos.length < 4) //BAD
      println("Error bad file.")
    val (id0, id1, length, damage) = (infos(0).toInt, infos(1).toInt,
                                      infos(2).toDouble, infos(3).toDouble)
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
