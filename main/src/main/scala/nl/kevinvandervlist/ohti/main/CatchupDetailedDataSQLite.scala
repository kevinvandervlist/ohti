package nl.kevinvandervlist.ohti.main

import java.util.UUID

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.ohti.api.PortalAPI
import nl.kevinvandervlist.ohti.api.model.IthoZonedDateTime
import nl.kevinvandervlist.ohti.config.Settings
import nl.kevinvandervlist.ohti.repository.MonitoringDataRepository
import nl.kevinvandervlist.ohti.repository.data.MonitoringDataValue
import nl.kevinvandervlist.ohti.usage.Catchup

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

object CatchupDetailedDataSQLite extends RunnableTask with LazyLogging with Catchup {
  override def name: String = "catchup-detailed-data-sqlite"

  override def apply(api: PortalAPI, settings: Settings)(implicit ec: ExecutionContext): Unit = {
    val aggCfg = settings.taskConfig(DetailedDataSQLite.name)
    val cfg = settings.taskConfig(name)
    // We gather all uuids from all known devices
    val _ids: Future[List[UUID]] = for {
      eds <- api.energyDevices()
      zs <- api.zones()
    } yield eds.devices.map(_.energyDeviceId) ++ zs.map(_.id)

    val maxDuration = 10000 millis
    val ids = Await.result(_ids, maxDuration)

    val from = IthoZonedDateTime.fromPortalString(s"${cfg.getString("from")}T11:00:00")
    val to = IthoZonedDateTime.fromPortalString(s"${cfg.getString("to")}T11:00:00")

    val repo = MonitoringDataRepository(aggCfg.getString("database"))

    // and retrieve all the monitoring data, and map those to
    // monitoring data values to be persisted in the repository
    for(d <- dailyRange(from, to)) {
      val data: Future[List[MonitoringDataValue]] = Future(ids)
        .map(_.map(api.retrieveDailyData(_, d)))
        .flatMap(lst => Future.sequence(lst))
        .map(_.flatten)
        .map(_.flatMap(MonitoringDataValue.toValues))

      val maxDuration = 10000 millis
      val result: List[MonitoringDataValue] = Await.result(data, maxDuration)
      logger.info("Got #{} rows of monitoring data for {}", result.size, d)

      repo.put(result) match {
        case Failure(exception) => logger.error("Failed to persist monitoring data: {}", exception)
        case Success(_) => logger.info(s"Successfully stored monitoring data values for {}", d)
      }
    }
  }
}
