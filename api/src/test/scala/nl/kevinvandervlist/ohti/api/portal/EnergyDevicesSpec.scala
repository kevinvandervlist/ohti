package nl.kevinvandervlist.ohti.api.portal

import java.util.UUID

import nl.kevinvandervlist.ohti.portal.{Endpoint, EnergyDevices}
import nl.kevinvandervlist.ohti.portal.TokenManager._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import sttp.client.Response
import sttp.client.testing.SttpBackendStub
import sttp.model.Method

class EnergyDevicesSpec extends AnyWordSpec with Matchers {
  private val devices =
    """[
      |  {"type":200,"scheduleId":null,"properties":[{"scheduleChoices":[{"label":"Aan","value":"1","disabled":false,"moments":null,"overrideUntil":0},{"label":"Uit","value":"0","disabled":false,"moments":null,"overrideUntil":0}],"label":"Powerplug","type":"choiceproperty","typeCustom":"choiceproperty-smartplugstatus","statusModified":false,"id":"SmartPlugStatus","status":null,"canControl":true,"hasLogging":false,"hasSchedule":true,"hasStatus":false,"isAvailable":null,"statusLastUpdated":null}],"parameters":null,"currentUsage":0.0,"historicUsage":null,"id":"45fba720-c04a-4b6c-a471-e9a5d5c0d3c3","name":"Gas Meter","serialNumber":"G0000000000001234","model":"SmartMeter","energyType":20,"isOnline":true,"isCentralMeter":true,"isDinRail":false,"isLiveUsageEnabled":false,"isProducer":false,"isSwitch":false,"isSwitchable":false,"isSwitchedOn":true,"meterIndexCurrentValue":1234.5000,"meterIndexTimestamp":"2020-01-31T13:45:00","meterIndexValue":1234.5000,"bdrSetting":0},
      |  {"type":200,"scheduleId":null,"properties":[{"scheduleChoices":[{"label":"Aan","value":"1","disabled":false,"moments":null,"overrideUntil":0},{"label":"Uit","value":"0","disabled":false,"moments":null,"overrideUntil":0}],"label":"Powerplug","type":"choiceproperty","typeCustom":"choiceproperty-smartplugstatus","statusModified":false,"id":"SmartPlugStatus","status":null,"canControl":true,"hasLogging":false,"hasSchedule":true,"hasStatus":false,"isAvailable":null,"statusLastUpdated":null}],"parameters":null,"currentUsage":123.0,"historicUsage":null,"id":"45fba720-c04a-4b6c-a471-e9a5d5c0d3c4","name":"Low Tariff Consumed","serialNumber":"E0000000000001234","model":"SmartMeter","energyType":10,"isOnline":true,"isCentralMeter":true,"isDinRail":false,"isLiveUsageEnabled":true,"isProducer":false,"isSwitch":false,"isSwitchable":false,"isSwitchedOn":true,"meterIndexCurrentValue":1234567.0000,"meterIndexTimestamp":"2020-01-31T13:45:00","meterIndexValue":1234567.0000,"bdrSetting":0},
      |  {"type":200,"scheduleId":null,"properties":[{"scheduleChoices":[{"label":"Aan","value":"1","disabled":false,"moments":null,"overrideUntil":0},{"label":"Uit","value":"0","disabled":false,"moments":null,"overrideUntil":0}],"label":"Powerplug","type":"choiceproperty","typeCustom":"choiceproperty-smartplugstatus","statusModified":false,"id":"SmartPlugStatus","status":null,"canControl":true,"hasLogging":false,"hasSchedule":true,"hasStatus":false,"isAvailable":null,"statusLastUpdated":null}],"parameters":null,"currentUsage":1234.0,"historicUsage":null,"id":"45fba720-c04a-4b6c-a471-e9a5d5c0d3c5","name":"Low Tariff Produced","serialNumber":"E0000000000001234","model":"SmartMeter","energyType":10,"isOnline":true,"isCentralMeter":true,"isDinRail":false,"isLiveUsageEnabled":true,"isProducer":true,"isSwitch":false,"isSwitchable":false,"isSwitchedOn":true,"meterIndexCurrentValue":1234321.0000,"meterIndexTimestamp":"2020-01-31T13:45:00","meterIndexValue":1234321.0000,"bdrSetting":0},
      |  {"type":200,"scheduleId":null,"properties":[{"scheduleChoices":[{"label":"Aan","value":"1","disabled":false,"moments":null,"overrideUntil":0},{"label":"Uit","value":"0","disabled":false,"moments":null,"overrideUntil":0}],"label":"Powerplug","type":"choiceproperty","typeCustom":"choiceproperty-smartplugstatus","statusModified":false,"id":"SmartPlugStatus","status":null,"canControl":true,"hasLogging":false,"hasSchedule":true,"hasStatus":false,"isAvailable":null,"statusLastUpdated":null}],"parameters":null,"currentUsage":0.0,"historicUsage":null,"id":"45fba720-c04a-4b6c-a471-e9a5d5c0d3c6","name":"Normal Tariff Consumed","serialNumber":"E0000000000001234","model":"SmartMeter","energyType":10,"isOnline":true,"isCentralMeter":true,"isDinRail":false,"isLiveUsageEnabled":true,"isProducer":false,"isSwitch":false,"isSwitchable":false,"isSwitchedOn":true,"meterIndexCurrentValue":4321567.0000,"meterIndexTimestamp":"2020-01-31T13:45:00","meterIndexValue":4321567.0000,"bdrSetting":0},
      |  {"type":200,"scheduleId":null,"properties":[{"scheduleChoices":[{"label":"Aan","value":"1","disabled":false,"moments":null,"overrideUntil":0},{"label":"Uit","value":"0","disabled":false,"moments":null,"overrideUntil":0}],"label":"Powerplug","type":"choiceproperty","typeCustom":"choiceproperty-smartplugstatus","statusModified":false,"id":"SmartPlugStatus","status":null,"canControl":true,"hasLogging":false,"hasSchedule":true,"hasStatus":false,"isAvailable":null,"statusLastUpdated":null}],"parameters":null,"currentUsage":0.0,"historicUsage":null,"id":"45fba720-c04a-4b6c-a471-e9a5d5c0d3c7","name":"Normal Tariff Produced","serialNumber":"E0000000000001234","model":"SmartMeter","energyType":10,"isOnline":true,"isCentralMeter":true,"isDinRail":false,"isLiveUsageEnabled":true,"isProducer":true,"isSwitch":false,"isSwitchable":false,"isSwitchedOn":true,"meterIndexCurrentValue":4321234.0000,"meterIndexTimestamp":"2020-01-31T13:45:00","meterIndexValue":4321234.0000,"bdrSetting":0},
      |  {"type":200,"scheduleId":null,"properties":[{"scheduleChoices":[{"label":"Aan","value":"1","disabled":false,"moments":null,"overrideUntil":0},{"label":"Uit","value":"0","disabled":false,"moments":null,"overrideUntil":0}],"label":"Powerplug","type":"choiceproperty","typeCustom":"choiceproperty-smartplugstatus","statusModified":false,"id":"SmartPlugStatus","status":null,"canControl":true,"hasLogging":false,"hasSchedule":true,"hasStatus":false,"isAvailable":null,"statusLastUpdated":null}],"parameters":null,"currentUsage":123.4,"historicUsage":null,"id":"45fba720-c04a-4b6c-a471-e9a5d5c0d3c8","name":"Some sensor","serialNumber":"12345678","model":"niko","energyType":10,"isOnline":true,"isCentralMeter":false,"isDinRail":true,"isLiveUsageEnabled":true,"isProducer":true,"isSwitch":false,"isSwitchable":false,"isSwitchedOn":true,"meterIndexCurrentValue":null,"meterIndexTimestamp":null,"meterIndexValue":null,"bdrSetting":0},
      |  {"type":200,"scheduleId":null,"properties":[{"scheduleChoices":[{"label":"Aan","value":"1","disabled":false,"moments":null,"overrideUntil":0},{"label":"Uit","value":"0","disabled":false,"moments":null,"overrideUntil":0}],"label":"Powerplug","type":"choiceproperty","typeCustom":"choiceproperty-smartplugstatus","statusModified":false,"id":"SmartPlugStatus","status":null,"canControl":true,"hasLogging":false,"hasSchedule":true,"hasStatus":false,"isAvailable":null,"statusLastUpdated":null}],"parameters":null,"currentUsage":456.7,"historicUsage":null,"id":"45fba720-c04a-4b6c-a471-e9a5d5c0d3c9","name":"Some other sensor","serialNumber":"01234567","model":"niko","energyType":10,"isOnline":true,"isCentralMeter":false,"isDinRail":true,"isLiveUsageEnabled":true,"isProducer":true,"isSwitch":true,"isSwitchable":true,"isSwitchedOn":true,"meterIndexCurrentValue":null,"meterIndexTimestamp":null,"meterIndexValue":null,"bdrSetting":0}
      |]""".stripMargin

  private implicit val endpoint: Endpoint = Endpoint("https://test.example.com")
  implicit val testingBackend = SttpBackendStub.synchronous
      .whenRequestMatchesPartial {
        case r if r.method == Method.GET => Response.ok(devices)
      }

  "Energy devices" should {
    "be retrieved when a token is present" in {
      implicit val tokenProvider: TokenProvider = () => Some(TokenResponse("access", "type", 10, "refresh"))
      val energyDevices = new EnergyDevices().retrieveDevices()
      energyDevices.isDefined shouldBe true
      energyDevices.get.size shouldBe 7
    }
    "not be retrieved without a token" in {
      implicit val tokenProvider: TokenProvider = () => None
      assertThrows[IllegalStateException] {
        new EnergyDevices().retrieveDevices()
      }
    }
  }
  "Rich energy devices" should {
    implicit val tokenProvider: TokenProvider = () => Some(TokenResponse("access", "type", 10, "refresh"))
    val energyDevices = new EnergyDevices().retrieveDevices().get
    "retrieve the gas meter(s)" in {
      val gas = energyDevices.gasMeters
      gas.size shouldBe 1
      gas.exists(_.id == UUID.fromString("45fba720-c04a-4b6c-a471-e9a5d5c0d3c3")) shouldBe true
    }
    "retrieve the central electric consumption meter(s)" in {
      val e = energyDevices.electricCentralMeterConsumption
      e.size shouldBe 2
      e.exists(_.id == UUID.fromString("45fba720-c04a-4b6c-a471-e9a5d5c0d3c4")) shouldBe true
      e.exists(_.id == UUID.fromString("45fba720-c04a-4b6c-a471-e9a5d5c0d3c6")) shouldBe true
    }
    "retrieve the central electric feedback meter(s)" in {
      val e = energyDevices.electricCentralMeterFeedback
      e.size shouldBe 2
      e.exists(_.id == UUID.fromString("45fba720-c04a-4b6c-a471-e9a5d5c0d3c5")) shouldBe true
      e.exists(_.id == UUID.fromString("45fba720-c04a-4b6c-a471-e9a5d5c0d3c7")) shouldBe true
    }
    "retrieve the central electric production meter(s)" in {
      val e = energyDevices.electricProduction
      e.size shouldBe 2
      e.exists(_.id == UUID.fromString("45fba720-c04a-4b6c-a471-e9a5d5c0d3c8")) shouldBe true
      e.exists(_.id == UUID.fromString("45fba720-c04a-4b6c-a471-e9a5d5c0d3c9")) shouldBe true
    }
  }
}