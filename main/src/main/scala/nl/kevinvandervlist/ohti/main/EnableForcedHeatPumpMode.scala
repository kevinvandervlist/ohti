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

object EnableForcedHeatPumpMode extends RunnableTask with LazyLogging {
  override def name: String = "enable-forced-heat-pump-mode"

  override def apply(api: PortalAPI, settings: Settings)(implicit ec: ExecutionContext): Unit = {
    // We asume this is the device id of the main device to configure schedules
    val devs: Future[Option[Device]] = api.retrieveDevices().map(_.collectFirst {
      case d: Device if d.hasCoolHeatChoice && d.hasFanSpeedControl => d
    })

    val maybeUpdatedSchedule: Future[Option[Schedule]] = devs
      .map(maybeDev => maybeDev.map(dev => api.retrieveSchedule(dev.id)))
      .flatMap(swapFuture)
      .map(_.flatMap(_.assignSetpoint(forcedHeatPumpSchedule)))
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
  private def forcedHeatPumpSchedule: List[ScheduledChoice] = List(
    ScheduledChoice("Standby", "18.5", isDisabled = false, Some(List(
      standby(DayOfWeek.MONDAY),
      standby(DayOfWeek.TUESDAY),
      standby(DayOfWeek.WEDNESDAY),
      standby(DayOfWeek.THURSDAY),
      standby(DayOfWeek.FRIDAY),
      standby(DayOfWeek.SATURDAY),
      standby(DayOfWeek.SUNDAY)
    ).flatten),0),
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

  private def standby(day: DayOfWeek): List[Moment] = List(
    Moment(day, 2700000),
    Moment(day, 9900000),
    Moment(day, 17100000),
    Moment(day, 26100000),
    Moment(day, 35100000),
    Moment(day, 44100000),
    Moment(day, 53100000),
    Moment(day, 62100000),
    Moment(day, 71100000),
    Moment(day, 80100000)
  )

  private def aanwezig(day: DayOfWeek): List[Moment] = List(
    Moment(day, 18900000),
    Moment(day, 27900000),
    Moment(day, 36900000),
    Moment(day, 45900000),
    Moment(day, 54900000),
    Moment(day, 63900000),
    Moment(day, 72900000)
  )

  private def afwezig(day: DayOfWeek): List[Moment] = List(
    Moment(day, 4500000),
    Moment(day, 11700000),
    Moment(day, 81900000)
  )
}
