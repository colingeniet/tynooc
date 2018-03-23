
object Good {

  var _id = 0

  sealed class Val(val id: Int) {
    _id += 1
    def this() { this(_id) }
  }
  
  object Chocolate extends Good.Val
  object Water extends Good.Val
  object IronOre extends Good.Val
  object Iron extends Good.Val
  object Wood extends Good.Val
  object Food extends Good.Val
  object Gaz extends Good.Val
  object Coal extends Good.Val
  object Oil extends Good.Val
  object Uranium extends Good.Val
}
