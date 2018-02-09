/* Graph manipulation */

/** A graph. */
trait Graph {
  /** A vertice of the graph. */
  trait Vertice {
    /** Iterate over all adjacent edges.
     * @param action the function called for each edge.
     *  takes the destination Vertice as parameter. */
    def iterateEdges(action: Vertice => Unit): Unit
  }
}


/** A graph with weighted edges.
 * @param Weight the weight type. */
trait WeightedGraph[Weight] extends Graph {
  /** A vertice of the graph. */
  trait WeightedVertice extends Vertice {
    /** Iterate over all adjacent edges.
     * @param action the function called for each edge.
     *  takes the destination Vertice and the edge weight as parameters. */
    def iterateWeightedEdges(action: (Vertice, Weight) => Unit): Unit

    /* Default implementation : use iterateWeightedEdges and
     * ignore weight parameter. */
    def iterateEdges(action: Vertice => Unit): Unit = {
      this.iterateWeightedEdges((vertice:Vertice, _) => action(vertice))
    }
  }
}

/** A point in the plane */
trait Position {
  val x: Double
  val y: Double
}

/** A graph with positionned vertices. */
trait PositionGraph extends Graph {
 /** A vertice of the graph. */
 trait PositionVertice extends Vertice with Position
}

/** A graph with weighted edges and positionned vertices.
 * @param Weight the weight type. */
trait PositionWeightedGraph[Weight]
extends WeightedGraph[Weight] with PositionGraph {
  /** A vertice of the graph. */
  trait PositionWeightedVertice extends WeightedVertice with PositionVertice
}
