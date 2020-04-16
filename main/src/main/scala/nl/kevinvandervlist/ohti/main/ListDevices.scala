package nl.kevinvandervlist.ohti.main

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.ohti.api.PortalAPI
import nl.kevinvandervlist.ohti.config.Config

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.language.postfixOps

object ListDevices extends App with LazyLogging {
  val cfg = Config()
  logger.info(s"Starting ohti for username ${cfg.username}...")
  val portal = PortalAPI(cfg.url, cfg.username, cfg.password, debug = true)

  val devices = Await.result(portal.energyDevices(), 5000 millis)
  println("Your devices are:")
  devices foreach { d =>
    println(s"- ${d.name}: ${d.energyType} -> ${d.id}")
  }

  portal.stop()
}
