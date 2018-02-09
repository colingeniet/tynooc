
class StateScene (p : FSMSCene) inherit Scene
{
  private val parent = p
  def goTo(s : StateScene) { p.changeScene(s) }
}

class FSMScene (init : StateScene) {
  
  var currentState = init
  def changeScene(s: StateScene) {currentState = s}
}