/** The trait of vertice in a graph represented using adjacency lists. */
trait Vertice {
  def iterateNeighbours(action: Vertice=>Unit): Unit
}

trait Position {
  val x: Double
  val y: Double
}

trait PositionnedVertice extends Vertice with Position {
}
