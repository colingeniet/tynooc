package parser

import scala.io.Source
import scala.util.matching.Regex
import logic.town._
import logic.world._
import logic.route._

/** An exception which could be laucnhed if the file can’t be parsed correctly. */
final case class BadFileFormatException(
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
 *  Town : name, x, y, welcomeLevel, <population, ...>
 *
 *  Route ::=
 *  Route : startId, endId, weight, damage
 *
 *  startId and endId are integers referencing the town ordering in the file.
 */
object Parser {
  private val rD = "(\\d+\\.?\\d*)"
  private val rI = "(\\d+)"
  private val rN = "([a-zA-Z0-9 ]+)"
  private val rIf = s"${rI}, "
  private val rTown = s"Town : ${rN}, ${rD}, ${rD}, ${rD}, "

  /** Parse a town line. 
    *
    * @param line The line string to parse.
    * @param world The world we are building.
    */
  private def parseTown(line: String, world: World): Town = {
    val rCompleteString = s"^${rTown}${rIf * (world.statusNumber - 1)}" + rI + "$"
    val reg = rCompleteString.r
    val town = line match {
      case reg (name, x, y, w, _*) => new Town(name, x.toDouble,
                                               y.toDouble, w.toDouble)
      case _                       => throw new BadFileFormatException
    }
    if(town.welcomingLevel > 1 || town.welcomingLevel < 0) throw new BadFileFormatException
    val pop = (rTown.r.replaceAllIn(line, "")).split(", ")
    pop.indices.foreach { i => town.addResidents(pop(i).toInt, world.status(i)) }
    town
  }

  /** Parse a route line. 
    * 
    * @param line The line string to parse.
    * @param towns The list of the towns of the world.
    */
  private def parseRoute(line: String, towns: List[Town]): Route = {
  var reg = (s"^Route : ${rI}, ${rI}, ${rD}, ${rD}" + "$").r
    line match {
      case reg(id0, id1, length, damage) => {
        new Route(towns(id0.toInt), towns(id1.toInt), length.toDouble, damage.toDouble)
      }
      case _                             => throw new BadFileFormatException
    }
  }

  /** Parse a world file. 
    *
    * @filename The path of the file to parse.
    */
  def readWorldInformations(filename: String): World = {
    val world = new World()
    var towns: List[Town] = List()
    var lines = Source.fromFile(filename).getLines.toList.filter { _(0) != '#'}
    while(lines.head(0) != 'R') {
      var town = parseTown(lines.head, world)
      towns = town :: towns
      world.addTown(town)
      lines = lines.tail
    }
    towns = towns.reverse
    lines.foreach { line =>
        var route = parseRoute(line, towns)
        route.start.addRoute(route)
    }
    world
  }
}
