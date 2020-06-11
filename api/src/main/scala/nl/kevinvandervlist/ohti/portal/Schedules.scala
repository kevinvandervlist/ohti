package nl.kevinvandervlist.ohti.portal

import java.time.DayOfWeek
import java.util.UUID

import io.circe.{Decoder, HCursor}
import nl.kevinvandervlist.ohti.api.model.{Moment, Schedule, ScheduledChoice, ScheduledDevice, ScheduledProperty}
import nl.kevinvandervlist.ohti.portal.TokenManager._
import nl.kevinvandervlist.ohti.portal.Schedules._
import sttp.client._
import sttp.client.circe._


object Schedules {
  implicit val decodeSchedule: Decoder[Schedule] = new Decoder[Schedule] {
    final def apply(c: HCursor): Decoder.Result[Schedule] = for {
      scheduleId <- c.downField("scheduleId").as[String]
      scheduledDevices <- c.downField("scheduledDevices").as[List[ScheduledDevice]]
      props <- c.downField("scheduledProperties").as[List[ScheduledProperty]]
    } yield {
      Schedule(UUID.fromString(scheduleId), scheduledDevices, props)
    }
  }

  implicit val decodeScheduledDevice: Decoder[ScheduledDevice] = new Decoder[ScheduledDevice] {
    final def apply(c: HCursor): Decoder.Result[ScheduledDevice] = for {
      id <- c.downField("id").as[String]
      active <- c.downField("active").as[Boolean]
    } yield {
      ScheduledDevice(UUID.fromString(id), active)
    }
  }

  implicit val decodeScheduledProperties: Decoder[ScheduledProperty] = new Decoder[ScheduledProperty] {
    final def apply(c: HCursor): Decoder.Result[ScheduledProperty] = for {
      id <- c.downField("id").as[String]
      choices <- c.downField("scheduleChoices").as[List[ScheduledChoice]]
    } yield {
      ScheduledProperty(id, choices)
    }
  }

  implicit val decodeScheduledChoice: Decoder[ScheduledChoice] = new Decoder[ScheduledChoice] {
    final def apply(c: HCursor): Decoder.Result[ScheduledChoice] = for {
      lbl <- c.downField("label").as[String]
      value <- c.downField("value").as[String]
      disabled <- c.downField("disabled").as[Boolean]
      moments <- c.downField("moments").as[List[Moment]]
    } yield {
      ScheduledChoice(lbl, value, disabled, moments)
    }
  }

  implicit val decodeMoment: Decoder[Moment] = new Decoder[Moment] {
    final def apply(c: HCursor): Decoder.Result[Moment] = for {
      day <- c.downField("day").as[DayOfWeek]
      time <- c.downField("time").as[Long]
    } yield {
      Moment(day, time)
    }
  }

  implicit val decodeDayOfWeek: Decoder[DayOfWeek] = new Decoder[DayOfWeek] {
    final def apply(c: HCursor): Decoder.Result[DayOfWeek] = for {
      day <- c.as[Int]
    } yield {
      day match {
        case 0 => DayOfWeek.MONDAY
        case 1 => DayOfWeek.TUESDAY
        case 2 => DayOfWeek.WEDNESDAY
        case 3 => DayOfWeek.THURSDAY
        case 4 => DayOfWeek.FRIDAY
        case 5 => DayOfWeek.SATURDAY
        case 6 => DayOfWeek.SUNDAY
      }
    }
  }
}

class Schedules(private implicit val endpoint: Endpoint,
                private implicit val tokenProvider: TokenProvider,
                protected implicit val backend: SttpBackend[Identity, Nothing, NothingT]) extends Client[nl.kevinvandervlist.ohti.api.model.Schedule] {

  def retrieveSchedule(id: UUID): Option[nl.kevinvandervlist.ohti.api.model.Schedule] = {
    val request = Util.authorizedRequest(tokenProvider)
      .get(endpoint.schedule(id.toString))

    val response = request
      .response(asJson[Schedule])

    doRequest("Failed to retrieve schedules, got code {} - {}", response)
  }
}
