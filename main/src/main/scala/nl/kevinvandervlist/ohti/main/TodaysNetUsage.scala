package nl.kevinvandervlist.ohti.main

import java.io.{File, PrintWriter}
import java.time.LocalDate
import java.util.{Date, UUID}

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.ohti.api.PortalAPI
import nl.kevinvandervlist.ohti.api.model.{IthoZonedDateTime, MonitoringData}
import nl.kevinvandervlist.ohti.config.Config
import nl.kevinvandervlist.ohti.usage.{Devices, RetrieveTotal, UsageInfo}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

object TodaysNetUsage extends App with LazyLogging {
  val cfg = Config()
  logger.info(s"Starting ohti for username ${cfg.username}...")
  val portal = PortalAPI(cfg.url, cfg.username, cfg.password, debug = cfg.debug)

  // These are manually defined
  val startOfYear = IthoZonedDateTime.fromPortalString("2020-01-01T11:00:00")
  val contractDate = IthoZonedDateTime.fromPortalString("2019-11-20T11:00:00")
  val devices: Devices = Devices(
    gas = List(
    ),
    consumed = List(
    ),
    produced = List(
    ),
    feedback = List(
    )
  )

  // The rest is derived automatically
  val oneDayAgo = IthoZonedDateTime.fromLocalDate(LocalDate.now.minusDays(1))
  val twoDaysAgo = IthoZonedDateTime.fromLocalDate(LocalDate.now.minusDays(2))
  val threeDaysAgo = IthoZonedDateTime.fromLocalDate(LocalDate.now.minusDays(3))
  val week = IthoZonedDateTime.fromLocalDate(LocalDate.now.minusDays(7))
  val month = IthoZonedDateTime.fromLocalDate(LocalDate.now.minusMonths(1))
  val quartile = IthoZonedDateTime.fromLocalDate(LocalDate.now.minusMonths(3))
  val year = IthoZonedDateTime.fromLocalDate(LocalDate.now.minusYears(1))

  val cases: Map[String, UUID => Future[MonitoringData]] = Map(
    "YTD" -> (portal.retrieveYearlyData(_, startOfYear)),
    "Contract" -> (portal.retrieveYearlyData(_, contractDate)),
    "Year" -> (portal.retrieveYearlyData(_, year)),
    "Quartile" -> (portal.retrieveQuarterlyData(_, quartile)),
    "Month" -> (portal.retrieveMonthlyData(_, month)),
    "Week" -> (portal.retrieveWeeklyData(_, week)),
    "Yesterday" -> (portal.retrieveDailyData(_, oneDayAgo)),
    "TwoDaysAgo" -> (portal.retrieveDailyData(_, twoDaysAgo)),
    "ThreeDaysAgo" -> (portal.retrieveDailyData(_, threeDaysAgo)),
  )

  val infos = Await.result(new RetrieveTotal(cases, devices).fetch(), 10000 millis)

  portal.stop()

  for(info <- infos) {
    println(info.name)
    println(s"- Gas: ${info.gas} m3")
    println(s"- Totaal verbruik: ${info.totalUsage} kWh (${info.ownProductionConsumptionPercentage}%)")
    println(s"- PV productie: ${info.produced} kWh")
    println(s"- Direct PV gebruik: ${info.directlyConsumedProduction} kWh (${info.directlyConsumedProductionPercentage}%)")
    println(s"- Netto teruglevering: ${info.credit} kWh")
    println(s"- Netto gebruik: ${info.consumed} kWh")
    println(s"- Stand salderen: ${info.compensated_net_usage} kWh")
  }

  val info = asJSON(infos)
  logger.info(info)

  val target = new File("data.json")
  logger.info(s"Writing data to ${target.getCanonicalPath}")
  dumpToFile(target, info)

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

