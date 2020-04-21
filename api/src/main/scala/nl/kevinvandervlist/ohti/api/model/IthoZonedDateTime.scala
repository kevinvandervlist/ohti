package nl.kevinvandervlist.ohti.api.model

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate, LocalTime, ZoneId, ZonedDateTime}
import java.util.Date

object IthoZonedDateTime {
  def today: IthoZonedDateTime = fromTimeStamp(System.currentTimeMillis())

  def fromPortalString(dateTime: String): IthoZonedDateTime = {
    val zdt = ZonedDateTime.parse(s"$dateTime+01:00[Europe/Amsterdam]")
    IthoZonedDateTime(zdt)
  }

  def fromTimeStamp(ts: Long): IthoZonedDateTime = {
    val zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.of("Europe/Amsterdam"))
    IthoZonedDateTime(zdt)
  }

  def fromDate(dt: Date): IthoZonedDateTime =
    fromTimeStamp(dt.getTime)

  def fromLocalDate(ld: LocalDate): IthoZonedDateTime =
    fromDate(Date.from(
      ld.atStartOfDay()
        .atZone(ZoneId.systemDefault())
        .toInstant
      )
    )
}

case class IthoZonedDateTime(private val zonedDateTime: ZonedDateTime) {
  def asTimeStamp: Long = zonedDateTime.toEpochSecond * 1000
  def asPortalString: String = zonedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
  def daysInMonth: Int = zonedDateTime.getMonth.length(zonedDateTime.toLocalDate.isLeapYear)

  // Why is this so complicate you ask? https://stackoverflow.com/a/29145886
  def startOfDay: IthoZonedDateTime =
    IthoZonedDateTime(zonedDateTime
      .toLocalDate
      .atStartOfDay()
      .atZone(zonedDateTime.getZone)
      .withLaterOffsetAtOverlap()
    )

  def endOfDay: IthoZonedDateTime =
    IthoZonedDateTime(zonedDateTime
      .`with`(LocalTime.of(23, 59, 59))
    )

  def between(other: IthoZonedDateTime, unit: ChronoUnit): Long =
   unit.between(zonedDateTime, other.zonedDateTime)
}
