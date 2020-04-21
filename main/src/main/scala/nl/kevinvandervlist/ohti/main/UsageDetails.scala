package nl.kevinvandervlist.ohti.main

import java.io.{File, PrintWriter}
import java.time.LocalDate
import java.util.UUID

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.ohti.api.PortalAPI
import nl.kevinvandervlist.ohti.api.model.{IthoZonedDateTime, MonitoringData}
import nl.kevinvandervlist.ohti.usage.{Devices, RetrieveTotal, UsageInfo}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

object UsageDetails extends RunnableTask with LazyLogging {
  override def name: String = "usage-details"

  // These are manually defined
  private val moveInDate = IthoZonedDateTime.fromPortalString("2017-12-08T11:00:00")
  private val contractDate = IthoZonedDateTime.fromPortalString("2019-11-20T11:00:00")
  private val devices: Devices = Devices(
    gas = List(
    ),
    consumed = List(
    ),
    produced = List(
    ),
    feedback = List(
    )
  )

  override def apply(api: PortalAPI)(implicit ec: ExecutionContext): Unit = {
    val startOfYear = IthoZonedDateTime.fromPortalString(s"${LocalDate.now().getYear}-01-01T11:00:00")
    val today = IthoZonedDateTime.fromLocalDate(LocalDate.now())
    val oneDayAgo = IthoZonedDateTime.fromLocalDate(LocalDate.now.minusDays(1))
    val twoDaysAgo = IthoZonedDateTime.fromLocalDate(LocalDate.now.minusDays(2))
    val threeDaysAgo = IthoZonedDateTime.fromLocalDate(LocalDate.now.minusDays(3))
    val week = IthoZonedDateTime.fromLocalDate(LocalDate.now.minusDays(7))
    val month = IthoZonedDateTime.fromLocalDate(LocalDate.now.minusMonths(1))
    val quartile = IthoZonedDateTime.fromLocalDate(LocalDate.now.minusMonths(3))
    val year = IthoZonedDateTime.fromLocalDate(LocalDate.now.minusYears(1))

    val cases: Map[String, UUID => Future[MonitoringData]] = Map(
      "Move-in" -> (api.retrieveYearlyData(_, moveInDate, today)),
      "YTD" -> (api.retrieveYearlyData(_, startOfYear)),
      "Contract" -> (api.retrieveYearlyData(_, contractDate)),
      "Year" -> (api.retrieveYearlyData(_, year)),
      "Quartile" -> (api.retrieveQuarterlyData(_, quartile)),
      "Month" -> (api.retrieveMonthlyData(_, month)),
      "Week" -> (api.retrieveWeeklyData(_, week)),
      "Yesterday" -> (api.retrieveDailyData(_, oneDayAgo)),
      "TwoDaysAgo" -> (api.retrieveDailyData(_, twoDaysAgo)),
      "ThreeDaysAgo" -> (api.retrieveDailyData(_, threeDaysAgo)),
    )

    val maxDuration = 30000 millis
    val infos = Await.result(new RetrieveTotal(cases, devices).fetch(), maxDuration)

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
