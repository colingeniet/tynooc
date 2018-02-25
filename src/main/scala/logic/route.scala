class Route(val start: Town, val end: Town, val weight: Double) extends Graph.Edge {
  
  def destination: Town = end
  def length: Double = weight  
}
