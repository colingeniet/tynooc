package gui.scenes.panes

import scalafx.Includes._
import scalafx.scene._
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.collections._
import scalafx.beans.property._

import logic.world._
import logic.town._
import logic.good._
import formatter._

class TownStats(towns: List[Town]) extends TableView(ObservableBuffer(towns)) {
  val nameColumn = new TableColumn[Town, String]("Town") {
    cellValueFactory = {
      c: TableColumn.CellDataFeatures[Town, String] => StringProperty(c.value.name)
    }
  }

  val goodColumns = Good.all.map{ g =>
    new TableColumn[Town, String](g.name) {
      cellValueFactory = {
        c: TableColumn.CellDataFeatures[Town, String] =>
          createStringBinding(
            () => f"${c.value.goods(g)()}%.0f (${MoneyFormatter.format(c.value.goods_prices(g)())})",
            c.value.goods(g),
            c.value.goods_prices(g))
      }
    }.delegate
  }

  columns += nameColumn
  columns ++= goodColumns
}
