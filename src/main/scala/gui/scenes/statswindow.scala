package gui.scenes

import scalafx.stage._
import scalafx.scene._

import gui.scenes.panes._
import logic.company._

class StatsWindow(companies: List[Company]) extends Stage {
  scene = new Scene {
    content = new CompaniesStats(companies)
  }
  title = "Tynooc - Companies Stats"
}
