package nl.kevinvandervlist.ohti.usage

case class UsageInfo(name: String, _gas: BigDecimal, _credit: BigDecimal, _consumed: BigDecimal, _produced: BigDecimal) {
  private def m3(bd: BigDecimal): String = bd.setScale(3, BigDecimal.RoundingMode.HALF_UP).toString()
  private def kwh(bd: BigDecimal): String = (bd / 1000).setScale(2, BigDecimal.RoundingMode.HALF_UP).toString()
  private def pct(bd: BigDecimal): String = bd.setScale(2, BigDecimal.RoundingMode.HALF_UP).toString()

  def gas: String = m3(_gas)
  def credit: String = kwh(_credit)
  def consumed: String = kwh(_consumed)
  def produced: String = kwh(_produced)
  def directlyConsumedProduction: String = kwh(_directlyConsumedProduction)
  def directlyConsumedProductionPercentage: String = pct(_directlyConsumedProductionPercentage)
  def ownProductionConsumptionPercentage: String = pct(_ownProductionConsumptionPercentage)
  def totalUsage: String = kwh(_totalUsage)
  def compensated_net_usage: String = kwh(_compensated_net_usage)

  def _directlyConsumedProduction: BigDecimal = _produced - _credit
  def _directlyConsumedProductionPercentage: BigDecimal = _directlyConsumedProduction / _produced * 100
  def _ownProductionConsumptionPercentage: BigDecimal = _directlyConsumedProduction / _totalUsage * 100
  def _totalUsage: BigDecimal = _directlyConsumedProduction + _consumed
  def _compensated_net_usage: BigDecimal = _consumed - _credit
}
