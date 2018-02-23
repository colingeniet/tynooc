/* Graph manipulation */
package logic.graph

import collection.mutable.HashMap
import collection.mutable.HashSet



/** A point in the plane
 */
trait Position {
  val x: Double
  val y: Double
}

/** A graph with weigthed edges and positioned vertices.
 */
trait Graph {
  /** The graph vertices
   */
  def vertices: List[Graph.Vertice]

  /** Find the shortest path between two vertices.
   *
   * @param from start vertice.
   * @param to   end vertice.
   * @return The list of vertices in the path, in order.
   */
  def findPath(from: Graph.Vertice, to: Graph.Vertice) : List[Graph.Vertice] = {
    // Dijkstra
    var closed: HashSet[Graph.Vertice] = new HashSet()
    var open: HashSet[Graph.Vertice] = new HashSet()
    var dist: HashMap[Graph.Vertice, Double] = new HashMap()
    var path: HashMap[Graph.Vertice, List[Graph.Vertice]] = new HashMap()
    dist(from) = 0
    open.add(from)
    path(from) = List()

    while(!open.isEmpty && !closed(to)) {
      var v: Graph.Vertice = open.minBy[Double](i => dist(i))
      v.incidentEdges.foreach { e =>
        if(!open(e.end) && !closed(e.end)) {
          open.add(e.end)
          dist(e.end) = dist(v) + e.weight
          path(e.end) = v :: path(v)
        } else if(open(e.end) && dist(e.end) > dist(v) + e.weight) {
          dist(e.end) = dist(v) + e.weight
          path(e.end) = v :: path(v)
        }
      }
      open.remove(v)
      closed.add(v)
    }

    path(to)
  }
}

/** Graph object companion.
 */
object Graph {
  /** A vertice of the graph. */
  trait Vertice extends Position {
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
    val weight: Double
  }
}
