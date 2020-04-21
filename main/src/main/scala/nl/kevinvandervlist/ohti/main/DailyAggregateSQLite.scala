package nl.kevinvandervlist.ohti.main

import java.time.LocalDate
import java.util.UUID

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.ohti.api.PortalAPI
import nl.kevinvandervlist.ohti.api.model.{IthoZonedDateTime, MonitoringData}
import nl.kevinvandervlist.ohti.config.Settings
import nl.kevinvandervlist.ohti.usage.{Devices, KnownDevices, RetrieveTotal}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

object DailyAggregateSQLite extends RunnableTask with KnownDevices with LazyLogging {
  override def name: String = "daily-aggregate-sqlite"

  override def apply(api: PortalAPI, settings: Settings)(implicit ec: ExecutionContext): Unit = {
    val devs: Devices = devices(settings.taskConfig(name))

    val yesterday = IthoZonedDateTime.fromLocalDate(LocalDate.now.minusDays(1))

    val cases: Map[String, UUID => Future[MonitoringData]] = Map(
      "Yesterday" -> (api.retrieveDailyData(_, yesterday)),
    )

    val maxDuration = 10000 millis
    val info = Await.result(new RetrieveTotal(cases, devs).fetch(), maxDuration).head

    logger.info("Daily aggregate: {}", info)
  }
}

