package nl.kevinvandervlist.ohti.main

import java.time.LocalDate

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.ohti.api.PortalAPI
import nl.kevinvandervlist.ohti.api.model.IthoZonedDateTime
import nl.kevinvandervlist.ohti.config.Settings
import nl.kevinvandervlist.ohti.repository.PeriodicUsageRepository
import nl.kevinvandervlist.ohti.usage.{Devices, PeriodicAggregateSQLite, RetrieveScenario}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps

object DailyAggregateSQLite extends RunnableTask with LazyLogging {
  override def name: String = "daily-aggregate-sqlite"

  override def apply(api: PortalAPI, settings: Settings)(implicit ec: ExecutionContext): Unit = {
    val cfg = settings.taskConfig(name)
    val devs = api.energyDevices().map(eds => Devices(
      gas = eds.gasMeters.map(_.id),
      consumed = eds.electricCentralMeterConsumption.map(_.id),
      produced = eds.electricProduction.map(_.id),
      feedback = eds.electricCentralMeterFeedback.map(_.id)
    ))
    val repo = PeriodicUsageRepository(cfg.getString("database"))
    val agg = devs.map(new PeriodicAggregateSQLite(repo, _))

    val yesterday = IthoZonedDateTime
      .fromLocalDate(LocalDate.now.minusDays(1))
      .startOfDay

    val scn = RetrieveScenario(yesterday.asPortalString.splitAt(10)._1, api.retrieveDailyData(_, yesterday), yesterday.startOfDay, yesterday.endOfDay)

    val maxDuration = 10000 millis
    val pu = Await.result(agg.flatMap(_.aggregate(scn)), maxDuration)
    logger.info("Daily aggregate: {}", pu)
  }
}
