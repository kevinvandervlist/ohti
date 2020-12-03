package nl.kevinvandervlist.ohti.main

import java.time.DayOfWeek

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.ohti.api.PortalAPI
import nl.kevinvandervlist.ohti.api.model.{Device, Moment, Schedule, ScheduledChoice}
import nl.kevinvandervlist.ohti.api.operations.DeviceOps._
import nl.kevinvandervlist.ohti.api.operations.ScheduleOps._
import nl.kevinvandervlist.ohti.config.Settings

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

object DisableForcedHeatPumpMode extends RunnableTask with LazyLogging {
  override def name: String = "disable-forced-heat-pump-mode"

  override def apply(api: PortalAPI, settings: Settings)(implicit ec: ExecutionContext): Unit = {
    // We asume this is the device id of the main device to configure schedules
    val devs: Future[Option[Device]] = api.retrieveDevices().map(_.collectFirst {
      case d: Device if d.hasCoolHeatChoice && d.hasFanSpeedControl => d
    })

    val maybeUpdatedSchedule: Future[Option[Schedule]] = devs
      .map(maybeDev => maybeDev.map(dev => api.retrieveSchedule(dev.id)))
      .flatMap(swapFuture)
      .map(_.flatMap(_.assignSetpoint(standardSchedule)))
      .map(_.map(plainSchedule))
      .map(_.map(api.updateSchedule))
      .flatMap(swapFuture)

    val maxDuration = 7500 millis
    val result = Await.result(maybeUpdatedSchedule, maxDuration)

    result match {
      case None => logger.info("Schedule has not been updated")
      case Some(schedule) => logger.info("Schedule {} has been updated", schedule.id)
    }
  }

  // https://stackoverflow.com/a/46473096
  private def swapFuture[T](o: Option[Future[T]])(implicit ec: ExecutionContext): Future[Option[T]] =
    o.map(_.map(Some(_))).getOrElse(Future.successful(None))

  // TODO: This should maybe be configurable, given it's my own schedule
  private def standardSchedule: List[ScheduledChoice] = List(
    ScheduledChoice("Standby", "18.5", isDisabled = false, Some(List(
    )),0),
    ScheduledChoice("Afwezig", "19", isDisabled = false, Some(List(
      afwezig(DayOfWeek.MONDAY),
      afwezig(DayOfWeek.TUESDAY),
      afwezig(DayOfWeek.WEDNESDAY),
      afwezig(DayOfWeek.THURSDAY),
      afwezig(DayOfWeek.FRIDAY),
      afwezig(DayOfWeek.SATURDAY),
      afwezig(DayOfWeek.SUNDAY)
    ).flatten),0),
    ScheduledChoice("Aanwezig", "19.5", isDisabled = false, Some(List(
      aanwezig(DayOfWeek.MONDAY),
      aanwezig(DayOfWeek.TUESDAY),
      aanwezig(DayOfWeek.WEDNESDAY),
      aanwezig(DayOfWeek.THURSDAY),
      aanwezig(DayOfWeek.FRIDAY),
      aanwezig(DayOfWeek.SATURDAY),
      aanwezig(DayOfWeek.SUNDAY)
    ).flatten),0),
    ScheduledChoice("Aangepast1", "20", isDisabled = true, Some(List(
    )),0),
    ScheduledChoice("Aangepast2", "20", isDisabled = true, Some(List(
    )),0))

  private def aanwezig(day: DayOfWeek): List[Moment] = List(
    Moment(day, 21600000)
  )

  private def afwezig(day: DayOfWeek): List[Moment] = List(
    Moment(day, 79200000)
  )
}
