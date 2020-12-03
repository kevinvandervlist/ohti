package nl.kevinvandervlist.ohti.portal

import java.time.DayOfWeek
import java.util.UUID

import io.circe.{Decoder, Encoder, HCursor, Json}
import nl.kevinvandervlist.ohti.api.model.{IthoZonedDateTime, Moment, Schedule, ScheduledChoice, ScheduledDevice, ScheduledProperty}
import nl.kevinvandervlist.ohti.portal.TokenManager._
import nl.kevinvandervlist.ohti.portal.Schedules._
import sttp.client._
import sttp.client.circe._

object Schedules {
  implicit val decodeSchedule: Decoder[Schedule] = new Decoder[Schedule] {
    final def apply(c: HCursor): Decoder.Result[Schedule] = for {
      deviceType <- c.downField("deviceType").as[Int]
      scheduleId <- c.downField("scheduleId").as[String]
      scheduledDevices <- c.downField("scheduledDevices").as[List[ScheduledDevice]]
      props <- c.downField("scheduledProperties").as[List[ScheduledProperty]]
      user <- c.downField("userId").as[String]
      createdAt <- c.downField("createdAt").as[Long]
      updatedAt <- c.downField("updatedAt").as[Long]
    } yield {
      Schedule(
        deviceType,
        UUID.fromString(scheduleId),
        scheduledDevices,
        props,
        user,
        IthoZonedDateTime.fromTimeStamp(createdAt),
        IthoZonedDateTime.fromTimeStamp(updatedAt)
      )
    }
  }

  implicit val encodeSchedule: Encoder[Schedule] = new Encoder[Schedule] {
    override def apply(s: Schedule): Json = Json.obj(
      ("deviceType", Json.fromInt(s.deviceType)),
      ("scheduleId", Json.fromString(s.id.toString)),
      ("scheduleName", Json.fromString(s.id.toString)),
      ("scheduledDevices", Json.arr(s.components.map(encodeScheduledDevice.apply): _*)),
      ("scheduledProperties", Json.arr(s.properties.map(encodeScheduledProperty.apply): _*)),
      ("createdBy", Json.fromString(s.user)),
      ("updatedBy", Json.fromString(s.user)),
      ("deletedBy", Json.Null),
      ("userId", Json.fromString(s.user)),
      ("createdAt", Json.fromLong(s.createdAt.asTimeStamp)),
      ("updatedAt", Json.fromLong(s.createdAt.asTimeStamp)),
      ("deletedAt", Json.fromInt(0)),
      ("id", Json.fromString(s.id.toString))
    )
  }

  implicit val decodeScheduledDevice: Decoder[ScheduledDevice] = new Decoder[ScheduledDevice] {
    final def apply(c: HCursor): Decoder.Result[ScheduledDevice] = for {
      id <- c.downField("id").as[String]
      active <- c.downField("active").as[Boolean]
    } yield {
      ScheduledDevice(UUID.fromString(id), active)
    }
  }

  implicit val encodeScheduledDevice: Encoder[ScheduledDevice] = new Encoder[ScheduledDevice] {
    override def apply(s: ScheduledDevice): Json = Json.obj(
      ("id", Json.fromString(s.id.toString)),
      ("active", Json.fromBoolean(s.isActive))
    )
  }

  implicit val decodeScheduledProperties: Decoder[ScheduledProperty] = new Decoder[ScheduledProperty] {
    final def apply(c: HCursor): Decoder.Result[ScheduledProperty] = for {
      id <- c.downField("id").as[String]
      choices <- c.downField("scheduleChoices").as[List[ScheduledChoice]]
    } yield {
      ScheduledProperty(id, choices)
    }
  }

  implicit val encodeScheduledProperty: Encoder[ScheduledProperty] = new Encoder[ScheduledProperty] {
    override def apply(s: ScheduledProperty): Json = Json.obj(
      ("id", Json.fromString(s.id)),
      ("scheduleChoices", Json.arr(s.choices.map(encodeScheduledChoice.apply): _*))
    )
  }

  implicit val decodeScheduledChoice: Decoder[ScheduledChoice] = new Decoder[ScheduledChoice] {
    final def apply(c: HCursor): Decoder.Result[ScheduledChoice] = for {
      lbl <- c.downField("label").as[String]
      value <- c.downField("value").as[String]
      disabled <- c.downField("disabled").as[Boolean]
      moments <- c.downField("moments").as[Option[List[Moment]]]
      overrideUntil <- c.downField("overrideUntil").as[Long]
    } yield {
      ScheduledChoice(lbl, value, disabled, moments, overrideUntil)
    }
  }

  implicit val encodeScheduledChoice: Encoder[ScheduledChoice] = new Encoder[ScheduledChoice] {
    override def apply(a: ScheduledChoice): Json = Json.obj(
      ("label", Json.fromString(a.label)),
      ("value", Json.fromString(a.value)),
      ("disabled", Json.fromBoolean(a.isDisabled)),
      ("moments", a.moments match {
        case Some(m) => Json.arr(m.map(encodeMoment.apply): _*)
        case None => Json.Null
      }),
      ("overrideUntil", Json.fromLong(a.overrideUntil)),
    )
  }

  implicit val decodeMoment: Decoder[Moment] = new Decoder[Moment] {
    final def apply(c: HCursor): Decoder.Result[Moment] = for {
      day <- c.downField("day").as[DayOfWeek]
      time <- c.downField("time").as[Long]
    } yield {
      Moment(day, time)
    }
  }

  implicit val encodeMoment: Encoder[Moment] = new Encoder[Moment] {
    override def apply(a: Moment): Json = Json.obj(
      ("day", encodeDayOfWeek(a.day)),
      ("time", Json.fromLong(a.time))
    )
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

  implicit val encodeDayOfWeek: Encoder[DayOfWeek] = new Encoder[DayOfWeek] {
    override def apply(a: DayOfWeek): Json = Json.fromInt(a.getValue - 1) // - 1 b/c iso8601
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

  def updateSchedule(schedule: nl.kevinvandervlist.ohti.api.model.Schedule): Option[nl.kevinvandervlist.ohti.api.model.Schedule] = {
    val request = Util.authorizedRequest(tokenProvider)
      .put(endpoint.schedule(schedule.id.toString))
      .body(schedule)

    val response = request
      .response(asJson[nl.kevinvandervlist.ohti.api.model.Schedule])

    doRequest("Failed to update schedule, got code {} - {}", response)
  }
}
