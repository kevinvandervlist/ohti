package nl.kevinvandervlist.ohti.main

import java.util.UUID

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
  val portal = PortalAPI(cfg.url, cfg.username, cfg.password, debug = true)

  val cases: Map[String, UUID => Future[MonitoringData]] = Map(
    "Contract" -> (portal.retrieveYearlyData(_, IthoZonedDateTime.fromPortalString("2019-11-20T11:00:00"))),
    "Year" -> (portal.retrieveYearlyData(_, IthoZonedDateTime.fromPortalString("2019-04-17T11:00:00"))),
    "Quarter" -> (portal.retrieveQuarterlyData(_, IthoZonedDateTime.fromPortalString("2020-01-17T11:00:00"))),
    "Month" -> (portal.retrieveMonthlyData(_, IthoZonedDateTime.fromPortalString("2020-03-17T11:00:00"))),
    "Week" -> (portal.retrieveWeeklyData(_, IthoZonedDateTime.fromPortalString("2020-04-10T11:00:00"))),
    "Yesterday" -> (portal.retrieveDailyData(_, IthoZonedDateTime.fromPortalString("2020-04-16T11:00:00"))),
  )

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

  val infos = Await.result(new RetrieveTotal(cases, devices).fetch(), 7500 millis)

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

  println(asJSON(infos))

  private def asJSON(infos: List[UsageInfo]): String = {
    s"""var usage = [
       |  ${infos.map(asJSON).mkString(",")}
       |];
       |""".stripMargin
  }

  private def asJSON(info: UsageInfo): String = {
    s"""{
       |  "name": "${info.name}",
       |  "gas": {
       |    "m3": ${info.gas},
       |  },
       |  "electricity": {
       |    "total_kwh": "${info.totalUsage}",
       |    "percentage_own_power": "${info.ownProductionConsumptionPercentage}",
       |    "total_production_kwh": "${info.produced}",
       |    "own_production_usage_kwh": "${info.directlyConsumedProduction}",
       |    "own_production_usage_percentage": "${info.directlyConsumedProductionPercentage}",
       |    "net_feedback_kwh": "${info.credit}",
       |    "net_usage_kwh": "${info.consumed}",
       |    "current_net_usage_ratio": "${info.compensated_net_usage}",
       |  }
       |}
       |""".stripMargin
  }
}

