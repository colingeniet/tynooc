package formatter

import java.util.{Currency, Locale}

/** An object which permitds to format money displaying. */ 
object MoneyFormatter {
  private val formatter = java.text.NumberFormat.getCurrencyInstance
  private val fr = Currency.getInstance(new Locale("fr", "FR"))
  formatter.setCurrency(fr)
  
  /** Formats <code>money</code> using euros.  
    *
    * @param money The money amount to format.
    */
  def format(money: Double): String = {
    formatter.format(money)
  }  
}