package nl.kevinvandervlist.ohti.usage

import nl.kevinvandervlist.ohti.repository.data.PeriodicUsage

case class UsageInfo(private val p: PeriodicUsage) {
  private def m3(bd: BigDecimal): String = bd.setScale(3, BigDecimal.RoundingMode.HALF_UP).toString()
  private def kwh(bd: BigDecimal): String = (bd / 1000).setScale(2, BigDecimal.RoundingMode.HALF_UP).toString()
  private def pct(bd: BigDecimal): String = bd.setScale(2, BigDecimal.RoundingMode.HALF_UP).toString()

  def name: String = p.name
  def gas: String = m3(p.gasUsage)
  def credit: String = kwh(p.creditedPower)
  def consumed: String = kwh(p.consumedPower)
  def produced: String = kwh(p.producedPower)
  def directlyConsumedProduction: String = kwh(p.directlyConsumedPowerProduction)
  def directlyConsumedProductionPercentage: String = pct(p.directlyConsumedProductionPowerUsagePercentage)
  def ownProductionConsumptionPercentage: String = pct(p.ownProductionPowerUsagePercentage)
  def totalUsage: String = kwh(p.totalUsagePower)
  def compensated_net_usage: String = kwh(p.compensatedPowerBalance)
}
