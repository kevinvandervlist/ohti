package nl.kevinvandervlist.ohti.main

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.ohti.api.PortalAPI
import nl.kevinvandervlist.ohti.config.Settings

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps

object ListDevices extends RunnableTask with LazyLogging {
  override def name: String = "list-devices"

  override def apply(api: PortalAPI, settings: Settings)(implicit ec: ExecutionContext): Unit = {
    val devices = Await.result(api.energyDevices(), 5000 millis)
    logger.info("Your devices are:")
    devices foreach { d =>
      logger.info(s"- ${d.name}: ${d.energyType} -> ${d.id}")
    }
  }
}
