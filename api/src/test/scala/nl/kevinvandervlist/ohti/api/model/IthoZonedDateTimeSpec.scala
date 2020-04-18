package nl.kevinvandervlist.ohti.api.model

import java.time.LocalDate
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

    "be mapped from a date object" in {
      val dt = new Date()
      val idt = IthoZonedDateTime.fromDate(dt)
      idt.startOfDay.asPortalString shouldBe s"${dt.getYear + 1900}-${String.format("%02d", dt.getMonth + 1)}-${dt.getDate}T00:00:00"
    }

    "be mapped from a local date object" in {
      val yesterday = LocalDate.now
      val dt = new Date()
      val idt = IthoZonedDateTime.fromLocalDate(yesterday)
      idt.startOfDay.asPortalString shouldBe s"${dt.getYear + 1900}-${String.format("%02d", dt.getMonth + 1)}-${dt.getDate}T00:00:00"
    }
  }
}

