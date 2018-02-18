package world

/* Graph manipulation */

/** A graph.
 */
trait Graph {
  /** The graph vertices
   */
  def vertices: List[Graph.Vertice]
}

/** Graph object companion.
 */
object Graph {
  /** A vertice of the graph. */
  trait Vertice {
    /** Incident edges list.
     *
     *  An edge is incident if its `start` is `this`.
     */
    def incidentEdges: List[Edge]
  }

  /** An edge of the graph.
   */
  trait Edge {
    val start: Vertice
    val end: Vertice
  }
}



/** A graph with weighted edges.
 *
 *  @param Weight the weight type.
 */
trait WeightedGraph[Weight] extends Graph {
  /** The graph vertices */
  def vertices: List[WeightedGraph.WeightedVertice[Weight]]
}

/** WeightedGraph object companion.
 */
object WeightedGraph {
  /** A vertice of the graph.
   *
   *  @param Weight the weight type.
   */
  trait WeightedVertice[Weight] extends Graph.Vertice {
    /** Incident edges list.
     *
     *  An edge is incident if its `start` is `this`.
     */
    def incidentEdges: List[WeightedEdge[Weight]]
  }

  /** An edge of the graph.
   *
   *  @param Weight the weight type.
   */
  trait WeightedEdge[Weight] extends Graph.Edge {
    val weight: Double
  }
}




/** A point in the plane
 */
trait Position {
  val x: Double
  val y: Double
}

/** A graph with positionned vertices.
 */
trait PositionGraph extends Graph {
  /** The graph vertices
   */
  def vertices: List[PositionGraph.PositionVertice]
}

/** PositionGraph object companion.
 */
object PositionGraph {
 /** A vertice of the graph.
  */
 trait PositionVertice extends Graph.Vertice with Position
 /** An edge of the graph.
  */
 trait Edge extends Graph.Edge
}



/** A graph with weighted edges and positionned vertices.
 *
 *  @param Weight the weight type.
 */
trait PositionWeightedGraph[Weight]
extends WeightedGraph[Weight] with PositionGraph {
  /** The graph vertices
   */
  def vertices: List[PositionWeightedGraph.PositionWeightedVertice[Weight]]
}

/** PositionWeightedGraph object companion.
 */
object PositionWeightedGraph {
  /** A vertice of the graph.
   *
   *  @param Weight the weight type.
   */
  trait PositionWeightedVertice[Weight]
  extends WeightedGraph.WeightedVertice[Weight] with PositionGraph.PositionVertice

  trait WeightedEdge[Weight] extends WeightedGraph.WeightedEdge[Weight]
}
