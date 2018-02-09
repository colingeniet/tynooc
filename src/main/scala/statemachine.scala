
class StateScene (p : FSMSCene) extends Scene
{
  private val parent = p
  def goTo(s : StateScene) { p.changeScene(s) }
}

class FSMScene (init : StateScene) {

  var currentState = init
  def changeScene(s: StateScene) {currentState = s}
}
