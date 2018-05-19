package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.layout._
import scalafx.scene.chart._
import scalafx.collections._

import logic.company._
import utils._

class CompaniesStats(companies: List[Company])
extends VBox(3) {
  private val statsData: List[javafx.scene.chart.XYChart.Series[Number, Number]] =
    companies.map(c => {
      val s = new XYChart.Series[Number, Number]()
      MapBind(c.moneyHistory, s.data(), {
        x: javafx.scene.chart.XYChart.Data[Number,Number] => x
      })
      s.name <== c.name
      s.delegate
    })

  private val xAxis = new NumberAxis {
    label = "time"
  }
  private val yAxis = new NumberAxis {
    label = "money"
  }
  xAxis.forceZeroInRange = false
  xAxis.animated = false

  private val stats: XYChart[Number, Number] = new LineChart(
    xAxis,
    yAxis,
    ObservableBuffer(statsData))

  children = stats
}
