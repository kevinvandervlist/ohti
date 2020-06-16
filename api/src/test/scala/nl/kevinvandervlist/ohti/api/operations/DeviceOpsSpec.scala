package nl.kevinvandervlist.ohti.api.operations

import java.util.UUID

import nl.kevinvandervlist.ohti.api.operations.DeviceOps._
import nl.kevinvandervlist.ohti.api.model.{Device, DeviceProperty, IthoZonedDateTime, ScheduledChoice}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DeviceOpsSpec extends AnyWordSpec with Matchers {
  val coolHeatProperties: List[DeviceProperty] = List(
    DeviceProperty(
      Some(List(
        ScheduledChoice(
          "Cool",
          "Cool",
          false,
          None,
          0
        ),
        ScheduledChoice(
          "Heat",
          "Heat",
          false,
          None,
          0
        )
      )),
      None,
      None,
      None,
      "abc",
      "tpe",
      "custom",
      statusModified = true,
      id = "abcxyz",
      status = Some("Heat"),
      canControl = true,
      hasLogging = false,
      hasSchedule = true,
      hasStatus = true,
      isAvailable = true,
      statusLastUpdated = Some(IthoZonedDateTime.today.startOfDay.asPortalString + ".00")
    )
  )
  val otherProperties: List[DeviceProperty] = List(
    DeviceProperty(
      Some(List(
        ScheduledChoice(
          "bb",
          "bb",
          false,
          None,
          0
        ),
        ScheduledChoice(
          "aa",
          "aa",
          false,
          None,
          0
        )
      )),
      None,
      None,
      None,
      "abc",
      "tpe",
      "custom",
      statusModified = true,
      id = "abcxyz",
      status = Some("xyz"),
      canControl = true,
      hasLogging = false,
      hasSchedule = true,
      hasStatus = true,
      isAvailable = true,
      statusLastUpdated = Some(IthoZonedDateTime.today.startOfDay.asPortalString + ".00")
    )
  )
  val coolHeatDev: Device = Device(
    0,
    None,
    UUID.randomUUID(),
    Some("name"),
    true,
    "model",
    "manufacturer",
    coolHeatProperties,
    Map.empty,
    100,
    1
  )
  val otherDev: Device = coolHeatDev.copy(properties = otherProperties)
  "A device with cool heat properties" should {
    "indicate that" in {
      coolHeatDev.hasCoolHeatChoice shouldBe true
    }
    "be able to configure heating choice, but be a noop" in {
      coolHeatDev.setHeatChoice().map(_.device) shouldBe None
    }
    "be able to configure cooling choice" in {
      coolHeatDev.setCoolChoice().map(_.device) should not be Some(coolHeatDev)
    }
    "configure cooling and then back to heating" in {
      val updated = coolHeatDev.setCoolChoice()
        .flatMap(_.setHeatChoice())
        .map(_.device)
        .get

      updated._etag shouldBe coolHeatDev._etag + 2
    }
  }
  "A device without heat properties" should {
    "indicate that" in {
      otherDev.hasCoolHeatChoice shouldBe false
    }
    "not be able to configure cooling choice" in {
      otherDev.setCoolChoice() shouldBe None
    }
    "not be able to configure heating choice" in {
      otherDev.setHeatChoice() shouldBe None
    }
  }
}
