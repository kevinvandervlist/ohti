package nl.kevinvandervlist.ohti.main

import java.time.LocalDate

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.ohti.api.PortalAPI
import nl.kevinvandervlist.ohti.api.model.IthoZonedDateTime
import nl.kevinvandervlist.ohti.config.Settings
import nl.kevinvandervlist.ohti.repository.PeriodicUsageRepository
import nl.kevinvandervlist.ohti.usage.{Devices, KnownDevices, PeriodicAggregateSQLite, RetrieveScenario, RetrieveTotal}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps

object DailyAggregateSQLite extends RunnableTask with KnownDevices with LazyLogging {
  override def name: String = "daily-aggregate-sqlite"

  override def apply(api: PortalAPI, settings: Settings)(implicit ec: ExecutionContext): Unit = {
    val cfg = settings.taskConfig(name)
    val devs: Devices = devices(cfg)
    val repo = PeriodicUsageRepository(cfg.getString("database"))
    val agg = new PeriodicAggregateSQLite(repo, devs)

    val yesterday = IthoZonedDateTime
      .fromLocalDate(LocalDate.now.minusDays(1))
      .startOfDay

    val scn = RetrieveScenario(yesterday.asPortalString.splitAt(10)._1, api.retrieveDailyData(_, yesterday), yesterday.startOfDay, yesterday.endOfDay)

    val maxDuration = 10000 millis
    val pu = Await.result(agg.aggregate(scn), maxDuration)
    logger.info("Daily aggregate: {}", pu)
  }
}
