package logic.town

import logic.graph._
import logic.routes._
import logic.game._

class Town(val name: String, val x: Double, val y: Double, val id,  
           val welcomingLevel: Double) extends Graph.Vertice {
           
  private var residents: Array[Int] = new Array(Game.world.StatusNumber)
  private var _routes: List[Route] = List()
  
  def routes: List[Route] = _routes
  def incidentEdges: List[Route] = routes
  def addRoute(route: Route): Unit = _routes = route :: _routes
  def neighbours: List[Town] = routes.map { _.end }
  def note: Double = welcomingLevel * population / Game.world.population
  def population: Int = residents.sum
  
  def addResidents(nb: Int, id: Int): Unit = town.residents[id] += nb
    
  def deleteResidents(nb: Int, id: Int): Unit = {
    if(nb > residents[id])
      throw new IllegalArgumentException("population should stay positive")
    residents[id] -= nb
  }
  
  def generateMigrant(to: Town): Int = Math.max(0, population * (to.note - note)
  
  def update(dt: Double): Unit = {
    p = population
    possibleDestinations = neighbours.sortBy { _.note }
    possibleDestinations.foreach { destination => 
      migrantNumber = generateMigrant(destination)
      val byStatus = residents.map { (migrantNumber * (_.self / p).toInt }
      Game.world.tryTravel(self, destination, byStatus)
    }
  }
}
