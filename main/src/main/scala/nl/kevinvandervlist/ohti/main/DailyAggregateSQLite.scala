package nl.kevinvandervlist.ohti.main

import java.time.LocalDate
import java.util.UUID

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.ohti.api.PortalAPI
import nl.kevinvandervlist.ohti.api.model.{IthoZonedDateTime, MonitoringData}
import nl.kevinvandervlist.ohti.config.Settings
import nl.kevinvandervlist.ohti.repository.PeriodicUsageRepository
import nl.kevinvandervlist.ohti.repository.data.{PeriodicUsage, TimeSpan}
import nl.kevinvandervlist.ohti.usage.{Devices, KnownDevices, RetrieveScenario, RetrieveTotal}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

object DailyAggregateSQLite extends RunnableTask with KnownDevices with LazyLogging {
  override def name: String = "daily-aggregate-sqlite"

  override def apply(api: PortalAPI, settings: Settings)(implicit ec: ExecutionContext): Unit = {
    val cfg = settings.taskConfig(name)
    val devs: Devices = devices(cfg)

    val yesterday = IthoZonedDateTime
      .fromLocalDate(LocalDate.now.minusDays(1))
      .startOfDay

    val cases: Map[String, RetrieveScenario] = Map(
      yesterday.asPortalString.splitAt(10)._1 -> RetrieveScenario(api.retrieveDailyData(_, yesterday), yesterday.startOfDay, yesterday.endOfDay),
    )
    val ts = TimeSpan(yesterday.startOfDay.asTimeStamp, yesterday.endOfDay.asTimeStamp)
    val repo = PeriodicUsageRepository(cfg.getString("database"))

    repo.exists(ts) match {
      case Failure(ex) => logger.error("The repository failed: {}", ex)
      case Success(true) => logger.info(s"Daily aggregate for {} already exists", ts)
      case Success(false) =>
        logger.info("Aggregated statistics for timespan {} do not exist yet, retrieving them.", ts)
        val maxDuration = 10000 millis
        val info = Await.result(new RetrieveTotal(cases, devs).fetch(), maxDuration).head

        repo.put(info) match {
          case Success(i) => logger.info(s"Persisted daily aggregate {}", i.name)
          case Failure(ex) => logger.error("Unable to persist daily aggregate: {}", ex)
        }
    }

    logger.info("Daily aggregate: {}", repo.get(ts).get)
  }
}

