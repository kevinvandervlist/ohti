package nl.kevinvandervlist.ohti.repository.data

import java.time.temporal.ChronoUnit
import java.util.UUID

import nl.kevinvandervlist.ohti.api.model.{CO2, IthoZonedDateTime, MonitoringData}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.language.implicitConversions

class MonitoringDataSpec extends AnyWordSpec with Matchers {
  private val now = IthoZonedDateTime.fromPortalString("2020-05-07T00:00:00")
  private val md = MonitoringData(UUID.randomUUID(), CO2, now, 15, now, List(
    None,
    Some(BigDecimal(1)),
    Some(BigDecimal(2.5))
  ))
  implicit def toLong(dt: IthoZonedDateTime): Long = dt.asTimeStamp

  "Monitoring data values" should {
    val values: List[MonitoringDataValue] = md
    "be mapped" in {
      values.size shouldBe 3
    }
    "contain the right values" in {
      values should contain theSameElementsAs List(
        MonitoringDataValue(
          MonitoringDataIndex(
            md.deviceId,
            now.add(0, ChronoUnit.MINUTES),
            now.add(15, ChronoUnit.MINUTES)
          ), null, "CO2"
        ),
        MonitoringDataValue(
          MonitoringDataIndex(
            md.deviceId,
            now.add(15, ChronoUnit.MINUTES),
            now.add(30, ChronoUnit.MINUTES)
          ), BigDecimal(1), "CO2"
        ),
        MonitoringDataValue(
          MonitoringDataIndex(
            md.deviceId,
            now.add(30, ChronoUnit.MINUTES),
            now.add(45, ChronoUnit.MINUTES)
          ), BigDecimal(2.5), "CO2"
        )
      )
    }
  }
}
