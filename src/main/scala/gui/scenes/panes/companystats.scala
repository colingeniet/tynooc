package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.layout._
import scalafx.scene.chart._
import scalafx.collections._

import logic.company._
import utils._


class CompanyStats(companies: List[Company])
extends HBox(10) {
  private val moneyData: List[javafx.scene.chart.XYChart.Series[Number, Number]] =
    companies.map(c => {
      val s = new XYChart.Series[Number, Number]()
      MapBind(c.moneyHistory, s.data(), {
        x: javafx.scene.chart.XYChart.Data[Number,Number] => x
      })
      s.name <== c.name
      s.delegate
    })

  private val vehiclesData: List[javafx.scene.chart.XYChart.Series[Number, Number]] =
    companies.map(c => {
      val s = new XYChart.Series[Number, Number]()
      MapBind(c.vehiclesHistory, s.data(), {
        x: javafx.scene.chart.XYChart.Data[Number,Number] => x
      })
      s.name <== c.name
      s.delegate
    })

  private val moneyXAxis = new NumberAxis {
    label = "time"
    forceZeroInRange = false
    animated = false
  }
  private val moneyYAxis = new NumberAxis {
    label = "money"
  }

  private val vehiclesXAxis = new NumberAxis {
    label = "time"
    forceZeroInRange = false
    animated = false
  }
  private val vehiclesYAxis = new NumberAxis {
    label = "vehicles"
  }

  private val moneyChart: XYChart[Number, Number] = new LineChart(
    moneyXAxis,
    moneyYAxis,
    ObservableBuffer(moneyData))

  private val vehiclesChart: XYChart[Number, Number] = new LineChart(
    vehiclesXAxis,
    vehiclesYAxis,
    ObservableBuffer(vehiclesData))

  children = List(moneyChart, vehiclesChart)
}
