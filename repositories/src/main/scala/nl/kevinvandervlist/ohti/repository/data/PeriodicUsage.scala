package nl.kevinvandervlist.ohti.repository.data

case class TimeSpan(from: Long, to: Long)

case class PeriodicUsage(timeSpan: TimeSpan,
                         name: String,
                         gasUsage: BigDecimal,
                         creditedPower: BigDecimal,
                         consumedPower: BigDecimal,
                         producedPower: BigDecimal,
                         directlyConsumedPowerProduction: BigDecimal,
                         directlyConsumedProductionPowerUsagePercentage: BigDecimal,
                         ownProductionPowerUsagePercentage: BigDecimal,
                         totalUsagePower: BigDecimal,
                         compensatedPowerBalance: BigDecimal)

object PeriodicUsage {
  def apply(timeSpan: TimeSpan,
            name: String,
            gasUsage: BigDecimal,
            creditedPower: BigDecimal,
            consumedPower: BigDecimal,
            producedPower: BigDecimal): PeriodicUsage = {

    val directlyConsumedProduction = producedPower - creditedPower
    val directlyConsumedProductionPowerUsagePercentage = directlyConsumedProduction / producedPower * 100
    val totalUsagePower: BigDecimal = directlyConsumedProduction + consumedPower
    val ownProductionPowerUsagePercentage: BigDecimal = directlyConsumedProduction / totalUsagePower * 100
    val compensatedPowerBalance: BigDecimal = consumedPower - creditedPower

    PeriodicUsage(
      timeSpan,
      name,
      gasUsage,
      creditedPower,
      consumedPower,
      producedPower,
      directlyConsumedProduction,
      directlyConsumedProductionPowerUsagePercentage,
      ownProductionPowerUsagePercentage,
      totalUsagePower,
      compensatedPowerBalance
    )
  }
}

