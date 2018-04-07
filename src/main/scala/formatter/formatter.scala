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

object TimeFormatter {
  /** Convert a time as a double to its string representation.
    *
    * Format : <Hours>h<Min>
    *
    *  @param t The <code>Double</code> value (is interpreted as hours).
    */
  def timeToHourString(t: Double): String =
    f"${t.floor}%02.0fh${t * 60 % 60}%02.0f"

  /** Convert a date as a double to its string representation.
    *
    *  Days numbering start from 1.
    *  Format : <Day> : <Hours>h<Min>
    *
    * @param t the <code>Doble</code> value (is interpreted as hours)
    */
  def timeToDateString(t: Double): String =
    f"day ${(t.toInt / 24 + 1)}%d : " + timeToHourString(t % 24)
}

object StringFormatter {
  /** Splits camelCase and snake_case.
   *
   *  Usefull to pretty print attribute names. */
  def casePrettyPrint(str: String): String = {
    str.replaceAll("([a-z])([A-Z])", "$1 $2").replace('_', ' ').toLowerCase()
  }
}
