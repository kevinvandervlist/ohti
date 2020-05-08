package nl.kevinvandervlist.ohti.main

import java.time.LocalDate
import java.util.UUID

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.ohti.api.PortalAPI
import nl.kevinvandervlist.ohti.api.model.IthoZonedDateTime
import nl.kevinvandervlist.ohti.config.Settings
import nl.kevinvandervlist.ohti.repository.data.MonitoringDataValue
import nl.kevinvandervlist.ohti.repository.MonitoringDataRepository

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

object DetailedDataSQLite extends RunnableTask with LazyLogging {
  override def name: String = "detailed-data-sqlite"

  override def apply(api: PortalAPI, settings: Settings)(implicit ec: ExecutionContext): Unit = {
    val cfg = settings.taskConfig(name)
    // We gather all uuids from all knows devices
    val ids: Future[List[UUID]] = for {
      eds <- api.energyDevices()
      zs <- api.zones()
    } yield eds.devices.map(_.id) ++ zs.map(_.id)

    // Take yesterdays data
    val yesterday = IthoZonedDateTime
      .fromLocalDate(LocalDate.now.minusDays(1))
      .startOfDay

    // and retrieve all the monitoring data, and map those to
    // monitoring data values to be persisted in the repository
    val data: Future[List[MonitoringDataValue]] = ids
      .map(_.map(api.retrieveDailyData(_, yesterday)))
      .flatMap(lst => Future.sequence(lst))
      .map(_.flatten)
      .map(_.flatMap(MonitoringDataValue.toValues))

    val repo = MonitoringDataRepository(cfg.getString("database"))

    val maxDuration = 10000 millis
    val result: List[MonitoringDataValue] = Await.result(data, maxDuration)
    logger.info("Got #{} rows of monitoring data", result.size)

    repo.put(result) match {
      case Failure(exception) => logger.error("Failed to persist monitoring data: {}", exception)
      case Success(_) => logger.info("Successfully stored monitoring data values")
    }
  }
}
