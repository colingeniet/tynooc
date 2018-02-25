package logic.town

import logic.graph._
import logic.routes._
import logic.game._
import logic.world._

class Town(val name: String, val x: Double, val y: Double, val id: Int,  
           val welcomingLevel: Double) extends Graph.Vertice {
           
  private var residents: Array[Int] = new Array(Game.world.statusNumber)
  private var _routes: List[Route] = List()
  
  def routes: List[Route] = _routes
  def incidentEdges: List[Route] = routes
  def addRoute(route: Route): Unit = _routes = route :: _routes
  def neighbours: List[Town] = routes.map { _.end }
  def note: Double = welcomingLevel * population / Game.world.population
  def population: Int = residents.sum
  
  def addResidents(nb: Int, status: Status.Val): Unit = residents(status.id) += nb
    
  def deleteResidents(nb: Int, status: Status.Val): Unit = {
    if(nb > residents(status.id))
      throw new IllegalArgumentException("population should stay positive")
    residents(status.id) -= nb
  }
  
  def generateMigrant(to: Town):Int = Math.max(0, population * (to.note - note)).toInt
  
  def update(dt: Double): Unit = {
    val p = population
    val possibleDestinations = neighbours.sortBy { _.note }
    possibleDestinations.foreach { destination => 
      val migrantNumber = generateMigrant(destination)
      val byStatus = residents.map { r => (migrantNumber * r / p).toInt }
      Game.world.tryTravel(this, destination, byStatus)
    }
  }
}
