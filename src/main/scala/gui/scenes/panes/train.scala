package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.event._

class Train extends ScrollPane {
  
  children = List(new Label("Train pane"), new Carriage, new Carriage)
}

object Carriage extends VBox {

	def desc() : List[Label] =
	{
		List(new Label("Test carriage"), new Label(0.toString))
	}
}

class Carriage extends VBox {

	children = Carriage.desc()

}