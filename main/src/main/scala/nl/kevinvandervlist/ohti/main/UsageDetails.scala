package nl.kevinvandervlist.ohti.main

import java.io.{File, PrintWriter}
import java.time.LocalDate

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.ohti.api.PortalAPI
import nl.kevinvandervlist.ohti.api.model.IthoZonedDateTime
import nl.kevinvandervlist.ohti.config.Settings
import nl.kevinvandervlist.ohti.main.DailyAggregateSQLite.devices
import nl.kevinvandervlist.ohti.usage.{Devices, RetrieveScenario, RetrieveTotal, UsageInfo}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

object UsageDetails extends RunnableTask with LazyLogging {
  override def name: String = "usage-details"

  override def apply(api: PortalAPI, settings: Settings)(implicit ec: ExecutionContext): Unit = {
    val cfg = settings.taskConfig(name)
    val devs: Devices = devices(cfg)

    val contractDate = IthoZonedDateTime.fromPortalString(cfg.getString("contract-date"))

    val startOfYear = IthoZonedDateTime.fromPortalString(s"${LocalDate.now().getYear}-01-01T11:00:00")
    val today = IthoZonedDateTime.fromLocalDate(LocalDate.now())
    val oneDayAgo = IthoZonedDateTime.fromLocalDate(LocalDate.now.minusDays(1))
    val twoDaysAgo = IthoZonedDateTime.fromLocalDate(LocalDate.now.minusDays(2))
    val threeDaysAgo = IthoZonedDateTime.fromLocalDate(LocalDate.now.minusDays(3))
    val week = IthoZonedDateTime.fromLocalDate(LocalDate.now.minusDays(7))
    val month = IthoZonedDateTime.fromLocalDate(LocalDate.now.minusMonths(1))
    val quartile = IthoZonedDateTime.fromLocalDate(LocalDate.now.minusMonths(3))
    val year = IthoZonedDateTime.fromLocalDate(LocalDate.now.minusYears(1))

    val y2019s = IthoZonedDateTime.fromPortalString("2019-01-01T11:00:00").startOfDay
    val y2019e = IthoZonedDateTime.fromPortalString("2019-12-31T11:00:00").endOfDay

    val cases = Set(
      RetrieveScenario("Yesterday", api.retrieveDailyData(_, oneDayAgo), oneDayAgo.startOfDay, oneDayAgo.endOfDay),
      RetrieveScenario("TwoDaysAgo", api.retrieveDailyData(_, twoDaysAgo), twoDaysAgo.startOfDay, twoDaysAgo.endOfDay),
      RetrieveScenario("ThreeDaysAgo", api.retrieveDailyData(_, threeDaysAgo), threeDaysAgo.startOfDay, threeDaysAgo.endOfDay),
      RetrieveScenario("Contract", api.retrieveYearlyData(_, contractDate), contractDate.startOfDay, today),
      RetrieveScenario("YTD", api.retrieveYearlyData(_, startOfYear), startOfYear.startOfDay, today),
      RetrieveScenario("Year", api.retrieveYearlyData(_, year), year.startOfDay, today),
      RetrieveScenario("Quartile", api.retrieveQuarterlyData(_, quartile), quartile.startOfDay, today),
      RetrieveScenario("Month", api.retrieveMonthlyData(_, month), month.startOfDay, today),
      RetrieveScenario("Week", api.retrieveWeeklyData(_, week), week.startOfDay, today),
      RetrieveScenario("2019", api.retrieveYearlyData(_, y2019s), y2019s, y2019e)
    )

    val maxDuration = 30000 millis
    val infos = Await
      .result(new RetrieveTotal(cases, devs).fetch(), maxDuration)
      .map(UsageInfo.apply)

    for(info <- infos) {
      logger.info(info.name)
      logger.info(s"- Gas: {} m3", info.gas)
      logger.info(s"- Totaal verbruik: {} kWh ({}%)", info.totalUsage, info.ownProductionConsumptionPercentage)
      logger.info(s"- PV productie: {} kWh", info.produced)
      logger.info(s"- Direct PV gebruik: {} kWh ({}%)", info.directlyConsumedProduction, info.directlyConsumedProductionPercentage)
      logger.info(s"- Netto teruglevering: {} kWh", info.credit)
      logger.info(s"- Netto gebruik: {} kWh", info.consumed)
      logger.info(s"- Stand salderen: {} kWh", info.compensated_net_usage)
    }

    val info = asJSON(infos)
    logger.info(info)

    val target = new File("data.json")
    logger.info(s"Writing data to ${target.getCanonicalPath}")
    dumpToFile(target, info)

  }

  private def asJSON(infos: List[UsageInfo]): String = {
    s"""[
       |  ${infos.map(asJSON).mkString(",")}
       |]
       |""".stripMargin
  }

  private def asJSON(info: UsageInfo): String = {
    s"""{
       |  "name": "${info.name}",
       |  "gas": {
       |    "m3": ${info.gas}
       |  },
       |  "electricity": {
       |    "total_kwh": ${info.totalUsage},
       |    "percentage_own_power": ${info.ownProductionConsumptionPercentage},
       |    "total_production_kwh": ${info.produced},
       |    "own_production_usage_kwh": ${info.directlyConsumedProduction},
       |    "own_production_usage_percentage": ${info.directlyConsumedProductionPercentage},
       |    "net_feedback_kwh": ${info.credit},
       |    "net_usage_kwh": ${info.consumed},
       |    "current_net_usage_ratio": ${info.compensated_net_usage}
       |  }
       |}
       |""".stripMargin
  }

  def dumpToFile(target: File, content: String): Unit = {
    val writer: PrintWriter = new PrintWriter(target)
    try {
      writer.write(content)
    } finally {
      writer.close()
    }
  }
}

