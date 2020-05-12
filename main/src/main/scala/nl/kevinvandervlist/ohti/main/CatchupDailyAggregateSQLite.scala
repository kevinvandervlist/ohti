package nl.kevinvandervlist.ohti.main

import java.time.temporal.ChronoUnit

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.ohti.api.PortalAPI
import nl.kevinvandervlist.ohti.api.model.IthoZonedDateTime
import nl.kevinvandervlist.ohti.config.Settings
import nl.kevinvandervlist.ohti.repository.PeriodicUsageRepository
import nl.kevinvandervlist.ohti.usage.{Catchup, Devices, PeriodicAggregateSQLite, RetrieveScenario}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps

object CatchupDailyAggregateSQLite extends RunnableTask with LazyLogging with Catchup {
  override def name: String = "catchup-daily-aggregate-sqlite"

  override def apply(api: PortalAPI, settings: Settings)(implicit ec: ExecutionContext): Unit = {
    // Note: we explicitly use the same config as daily aggregate sqlite since this acts as an extension
    val aggCfg = settings.taskConfig(DailyAggregateSQLite.name)
    val cfg = settings.taskConfig(name)
    val devs = api.energyDevices().map(eds => Devices(
      gas = eds.gasMeters.map(_.id),
      consumed = eds.electricCentralMeterConsumption.map(_.id),
      produced = eds.electricProduction.map(_.id),
      feedback = eds.electricCentralMeterFeedback.map(_.id)
    ))
    val repo = PeriodicUsageRepository(aggCfg.getString("database"))
    val agg = devs.map(new PeriodicAggregateSQLite(repo, _))

    val from = IthoZonedDateTime.fromPortalString(s"${cfg.getString("from")}T11:00:00")
    val to = IthoZonedDateTime.fromPortalString(s"${cfg.getString("to")}T11:00:00")
    for(d <- dailyRange(from, to)) {
      val name = d.asPortalString.splitAt(10)._1
      val scn = RetrieveScenario(name, api.retrieveDailyData(_, d), d.startOfDay, d.endOfDay)

      val maxDuration = 10000 millis
      val pu = Await.result(agg.flatMap(_.aggregate(scn)), maxDuration)
      logger.info("Daily aggregate: {}", pu)
    }
  }
}
