package gui.scenes

import scalafx.stage._
import scalafx.scene._
import scalafx.scene.layout._
import scalafx.scene.control._

import gui.scenes.panes._
import logic.company._
import logic.town._

class CompanyStatsWindow(companies: List[Company]) extends Stage {
  scene = new Scene {
    content = new CompanyStats(companies)
  }
  title = "Tynooc - Companies Stats"
}

class TownStatsWindow(towns: List[Town]) extends Stage {
  scene = new Scene {
    content = new ScrollPane {
      content = new TownStats(towns)
    }
  }
  title = "Tynooc - Towns Stats"
}
