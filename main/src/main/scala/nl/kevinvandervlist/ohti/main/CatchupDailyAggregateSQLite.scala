package nl.kevinvandervlist.ohti.main

import java.time.temporal.ChronoUnit

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.ohti.api.PortalAPI
import nl.kevinvandervlist.ohti.api.model.IthoZonedDateTime
import nl.kevinvandervlist.ohti.config.Settings
import nl.kevinvandervlist.ohti.repository.PeriodicUsageRepository
import nl.kevinvandervlist.ohti.usage.{Devices, KnownDevices, PeriodicAggregateSQLite, RetrieveScenario}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps

object CatchupDailyAggregateSQLite extends RunnableTask with KnownDevices with LazyLogging {
  override def name: String = "catchup-daily-aggregate-sqlite"

  override def apply(api: PortalAPI, settings: Settings)(implicit ec: ExecutionContext): Unit = {
    // Note: we explicitly use the same config as daily aggregate sqlite since this acts as an extension
    val aggCfg = settings.taskConfig(DailyAggregateSQLite.name)
    val cfg = settings.taskConfig(name)
    val devs: Devices = devices(aggCfg)
    val repo = PeriodicUsageRepository(aggCfg.getString("database"))
    val agg = new PeriodicAggregateSQLite(repo, devs)

    val from = IthoZonedDateTime.fromPortalString(s"${cfg.getString("from")}T11:00:00")
    val to = IthoZonedDateTime.fromPortalString(s"${cfg.getString("to")}T11:00:00")
    for(d <- range(from, to)) {
      val name = d.asPortalString.splitAt(10)._1
      val scn = RetrieveScenario(name, api.retrieveDailyData(_, d), d.startOfDay, d.endOfDay)

      val maxDuration = 10000 millis
      val pu = Await.result(agg.aggregate(scn), maxDuration)
      logger.info("Daily aggregate: {}", pu)
    }
  }

  private def range(start: IthoZonedDateTime, inclusiveEnd: IthoZonedDateTime): Iterable[IthoZonedDateTime] = new Iterable[IthoZonedDateTime] {
    override def iterator: Iterator[IthoZonedDateTime] = new Iterator[IthoZonedDateTime] {
      private var cur: IthoZonedDateTime = start
      private val end = inclusiveEnd.add(1, ChronoUnit.DAYS).endOfDay
      override def hasNext: Boolean = cur.endOfDay != end

      override def next(): IthoZonedDateTime = {
        val ret = cur
        cur = ret.add(1, ChronoUnit.DAYS)
        ret
      }
    }
  }
}
