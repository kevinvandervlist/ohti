package nl.kevinvandervlist.ohti.main

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.ohti.api.PortalAPI
import nl.kevinvandervlist.ohti.api.operations.DeviceOps._
import nl.kevinvandervlist.ohti.api.model.Device
import nl.kevinvandervlist.ohti.config.Settings

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

object EnableCoolingMode extends RunnableTask with LazyLogging {
  override def name: String = "enable-cooling-mode"

  override def apply(api: PortalAPI, settings: Settings)(implicit ec: ExecutionContext): Unit = {
    val devs: Future[Option[Device]] = api.retrieveDevices().map(_.collectFirst {
      case d: Device if d.hasCoolHeatChoice && d.hasFanSpeedControl => d
    })

    val set: Future[Either[String, Device]] = devs.flatMap {
      case None => Future(Left("Cannot find a device with cooling mode!"))
      case Some(dev) => dev.setCoolChoice() match {
        case None => Future(Left("Cannot update device state. Is it in cooling mode already?"))
        case Some(updated) => api.updateDevice(updated).map(Right.apply)
      }
    }

    val maxDuration = 3000 millis
    val result = Await.result(set, maxDuration)

    result match {
      case Left(msg) => logger.info("Something went wrong: {}", msg)
      case Right(d) => logger.info("Successfully updated device {}: cooling is now enabled", d.id)
    }
  }
}
