package parser

import scala.io.Source
import logic.town._
import logic.world._
import logic.route._

/** An exception which could be laucnhed if the file can’t be parsed correctly. */
final case class BadFileFormat(
  private val message: String = "",
  private val cause: Throwable = None.orNull)
extends Exception(message, cause)

/** World file parser. */
/* Format :
 *  File ::=
 *  [Towns ...]
 *  #
 *  [Routes ...]
 *
 *  Towns ::=
 *  name, x, y, welcomeLevel, <population, ...>
 *
 *  Route ::=
 *  startId, endId, weight, damage
 *
 *  startId and endId are integers referencing the town ordering in the file.
 */
/* This is a temporary parser to avoid hard coding the world map
 * for this first version of the project. It WILL be replaced by
 * a proper parser in later versions. */
object Parser {
  /** Parse a town line. */
  private def parseTown(line: String, world: World): Town = {
    val infos = line.split(", ")
    if(infos.length < 4 + world.statusNumber)
      throw new BadFileFormat
    val (name, x, y, w) = (infos(0), infos(1).toDouble, infos(2).toDouble,
                           infos(3).toDouble)
    val p = infos.slice(4, world.statusNumber + 5)
    val town = new Town(name, x, y, w)
    for(i <- 0 to world.statusNumber - 1) {
      town.addResidents(p(i).toInt, world.status(0))
    }
    town
  }

  /** Parse a route line. */
  private def parseRoute(line: String, towns: List[Town]): Route = {
    val infos = line.split(", ")
    if(infos.length < 4)
      throw new BadFileFormat
    val (id0, id1, length, damage) = (infos(0).toInt, infos(1).toInt,
                                      infos(2).toDouble, infos(3).toDouble)
    val (start, end) = (towns(id0), towns(id1))
    new Route(start, end, length, damage)
  }

  /** Parse a world file. */
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
