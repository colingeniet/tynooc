package formatter

import java.util.{Currency, Locale}

object MoneyFormatter {
  private val formatter = java.text.NumberFormat.getCurrencyInstance
  private val fr = Currency.getInstance(new Locale("fr", "FR"))
  formatter.setCurrency(fr)
  
  def format(money: Double): String = {
    formatter.format(money)
  }  
}