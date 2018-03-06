package formatter

import java.util.{Currency, Locale}

/** An object which permitds to format money displaying. */
object MoneyFormatter {
  private val formatter = new java.text.DecimalFormat("€#,###.##;€-#,###.##")

  /** Formats <code>money</code> using euros.
    *
    * @param money The money amount to format.
    */
  def format(money: Double): String = {
    formatter.format(money)
  }
}
