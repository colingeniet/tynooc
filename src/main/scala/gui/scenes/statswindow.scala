package gui.scenes

import scalafx.stage._
import scalafx.scene._
import scalafx.scene.layout._
import scalafx.scene.control._
import javafx.event._

import gui.scenes.panes._
import logic.company._
import logic.town._

class CompanyStatsWindow(companies: List[Company]) extends Stage {
  scene = new Scene {
    root = new CompanyStats(companies)
  }
  title = "Tynooc - Companies Stats"

  // minimize instead of closing
  onCloseRequest = new EventHandler[javafx.stage.WindowEvent]() {
      def handle(event: javafx.stage.WindowEvent): Unit = {
        event.consume()
        delegate.setIconified(true) // don't ask me why this is not in scalafx
      }
    }
}

class TownStatsWindow(towns: List[Town]) extends Stage {
  scene = new Scene {
    root = new ScrollPane {
      content = new TownStats(towns)
      fitToHeight = true
      fitToWidth = true
    }
  }
  title = "Tynooc - Towns Stats"

  // minimize instead of closing
  onCloseRequest = new EventHandler[javafx.stage.WindowEvent]() {
      def handle(event: javafx.stage.WindowEvent): Unit = {
        event.consume()
        delegate.setIconified(true)
      }
    }
}
