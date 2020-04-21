package nl.kevinvandervlist.ohti.main

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.ohti.config.Config
import nl.kevinvandervlist.ohti.api.PortalAPI
import nl.kevinvandervlist.ohti.api.model.{IthoZonedDateTime, MonitoringData}

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Success

object Main extends App with LazyLogging {
  val cfg = Config()
  logger.info(s"Starting ohti for username ${cfg.username}...")
  val portal = PortalAPI(cfg.url, cfg.username, cfg.password, debug = cfg.debug)

  val result: Future[List[MonitoringData]] = portal.energyDevices().map(eds => {
    val today = IthoZonedDateTime.today
    eds.map(ed => portal.retrieveDailyData(ed.id, today))
  }).map(flist => Future.sequence(flist.map(_.transform(Success(_)))))
    .flatMap(_.map(_.collect{ case Success(data) => data }))


  val measurements = Await.result(result, 5000 millis)
  measurements foreach { m =>
    println(m)
  }

  println("Done consuming measurements")

  portal.stop()
}
