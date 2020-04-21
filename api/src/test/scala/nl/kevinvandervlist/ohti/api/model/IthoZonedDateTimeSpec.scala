package nl.kevinvandervlist.ohti.api.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Date

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class IthoZonedDateTimeSpec extends AnyWordSpec with Matchers {
  "Date and time data" should {
    "be symmetrical in case of string" in {
      val input = "2020-03-16T11:00:00"
      val izdt = IthoZonedDateTime.fromPortalString(input)
      izdt.asPortalString shouldBe input
    }

    "be symmetrical in case of timestamp" in {
      val input = 1583056800000L
      val izdt = IthoZonedDateTime.fromTimeStamp(input)
      izdt.asTimeStamp shouldBe input
    }

    "retrieve the start of a given day" in {
      val izdt = IthoZonedDateTime.fromPortalString("2020-02-29T11:32:00")
      izdt.startOfDay shouldBe IthoZonedDateTime.fromPortalString("2020-02-29T00:00:00")
    }

    "retrieve the end of a given day" in {
      val izdt = IthoZonedDateTime.fromPortalString("2020-02-29T11:32:00")
      izdt.endOfDay shouldBe IthoZonedDateTime.fromPortalString("2020-02-29T23:59:59")
    }

    "be mapped from a date object" in {
      val now = LocalDate.now()
      val idt = IthoZonedDateTime.fromDate(new Date())
      idt.startOfDay.asPortalString shouldBe s"${now.getYear}-${String.format("%02d", now.getMonth.getValue)}-${now.getDayOfMonth}T00:00:00"
    }

    "be mapped from a local date object" in {
      val yesterday = LocalDate.now
      val idt = IthoZonedDateTime.fromLocalDate(yesterday)
      idt.startOfDay.asPortalString shouldBe s"${yesterday.getYear}-${String.format("%02d", yesterday.getMonth.getValue)}-${yesterday.getDayOfMonth}T00:00:00"
    }

    "calculate right delta between two zdts" in {
      val a = IthoZonedDateTime.fromPortalString("2019-03-16T11:00:00")
      val b = IthoZonedDateTime.fromPortalString("2020-04-01T11:00:00")
      a.between(b, ChronoUnit.WEEKS) shouldBe 54
      a.between(b, ChronoUnit.DAYS) shouldBe 382
    }
  }
}

