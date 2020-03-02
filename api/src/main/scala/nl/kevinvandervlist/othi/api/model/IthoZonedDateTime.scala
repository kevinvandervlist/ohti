package nl.kevinvandervlist.othi.api.model

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId, ZonedDateTime}

object IthoZonedDateTime {
  def fromPortalString(dateTime: String): IthoZonedDateTime = {
    val zdt = ZonedDateTime.parse(s"$dateTime+01:00[Europe/Amsterdam]")
    IthoZonedDateTime(zdt)
  }

  def fromTimeStamp(ts: Long): IthoZonedDateTime = {
    val zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.of("Europe/Amsterdam"))
    IthoZonedDateTime(zdt)
  }
}

case class IthoZonedDateTime(private val zonedDateTime: ZonedDateTime) {
  def asTimeStamp: Long = zonedDateTime.toEpochSecond * 1000
  def asPortalString: String = zonedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
  def daysInMonth: Int = zonedDateTime.getMonth.length(zonedDateTime.toLocalDate.isLeapYear)

  // Why is this so complicate you ask? https://stackoverflow.com/a/29145886
  def startOfDay: IthoZonedDateTime = IthoZonedDateTime(zonedDateTime.toLocalDate.atStartOfDay().atZone(zonedDateTime.getZone).withLaterOffsetAtOverlap())
}
