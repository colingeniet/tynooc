/* Graph manipulation */

/** A graph with weighted edges.
 * @param Weight the weight type. */
trait WeightedGraph[Weight] {
  /** A vertice of the graph. */
  trait Vertice {
    /** Iterate over all adjacent edges.
     * @param action the function called for each edge.
     *  takes the destination Vertice and the edge weight as parameters. */
    def iterateEdges(action: (Vertice, Weight) => Unit): Unit
  }
}

/** A point in the plane */
trait Position {
  val x: Double
  val y: Double
}

/** A graph with weighted edges and positioned vertices.
 * @param Weight the weight type. */
trait PositionWeightedGraph[Weight] extends WeightedGraph[Weight] {
  /** A vertice of the graph. */
  trait PositionVertice extends Vertice with Position
}
