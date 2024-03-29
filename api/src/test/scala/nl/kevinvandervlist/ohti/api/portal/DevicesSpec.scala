package nl.kevinvandervlist.ohti.api.portal

import java.util.UUID

import nl.kevinvandervlist.ohti.api.model.Device
import nl.kevinvandervlist.ohti.portal.TokenManager._
import nl.kevinvandervlist.ohti.portal.{Devices, Endpoint}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import sttp.client3.Response
import sttp.client3.testing.SttpBackendStub
import sttp.model.Method

class DevicesSpec extends AnyWordSpec with Matchers {
  private val singleDevice =
    """{
      |  "type":102,
      |  "scheduleId":null,
      |  "id":"45fba720-c04a-4b6c-a471-e9a5d5c0d3c4",
      |  "name":null,
      |  "isOnline":false,
      |  "model":"dsmrbrick",
      |  "manufacturer":"fifthplay",
      |  "properties":[],
      |  "parameters":{},
      |  "_etag":"12345678",
      |  "bdrSetting":0
      |}""".stripMargin

  private val allDevices =
    """[
      |  {
      |    "type":102,
      |    "scheduleId":null,
      |    "id":"45fba720-c04a-4b6c-a471-e9a5d5c0d3c4",
      |    "name":null,
      |    "isOnline":false,
      |    "model":"dsmrbrick",
      |    "manufacturer":"fifthplay",
      |    "properties":[],
      |    "parameters":{},
      |    "_etag":"12345678",
      |    "bdrSetting":0
      |  },
      |  {
      |    "type":105,
      |    "scheduleId":"45fba720-c04a-4b6c-a471-e9a5d5c0d3c5",
      |    "id":"45fba720-c04a-4b6c-a471-e9a5d5c0d3c5",
      |    "name":"Thermostat",
      |    "isOnline":true,
      |    "model":"generic",
      |    "manufacturer":"spider",
      |    "properties":[
      |      {
      |        "scheduleChoices":[
      |          {
      |            "label":"Aan",
      |            "value":"True",
      |            "disabled":false,
      |            "moments":null,
      |            "overrideUntil":0
      |          }
      |        ],
      |        "label":"FilterDirty",
      |        "type":"booleanproperty",
      |        "typeCustom":"booleanproperty",
      |        "statusModified":false,
      |        "id":"FilterDirty",
      |        "status":"false",
      |        "canControl":false,
      |        "hasLogging":false,
      |        "hasSchedule":false,
      |        "hasStatus":true,
      |        "isAvailable":true,
      |        "statusLastUpdated":"2020-05-30T05:24:37.66"
      |      },
      |      {
      |        "scheduleChoices":[
      |          {
      |            "label":"-",
      |            "value":"Away",
      |            "disabled":false,
      |            "moments":null,
      |            "overrideUntil":0
      |          }
      |        ],
      |        "label":"Ventilatorsnelheid",
      |        "type":"choiceproperty",
      |        "typeCustom":"choiceproperty-fanspeed",
      |        "statusModified":false,
      |        "id":"FanSpeed",
      |        "status":"Auto",
      |        "canControl":true,
      |        "hasLogging":false,
      |        "hasSchedule":true,
      |        "hasStatus":true,
      |        "isAvailable":true,
      |        "statusLastUpdated":"2020-05-30T17:25:03.517"
      |      },
      |      {
      |        "scheduleChoices":[
      |          {
      |            "label":"Scheduled",
      |            "value":"Scheduled",
      |            "disabled":false,
      |            "moments":null,
      |            "overrideUntil":0
      |          }
      |        ],
      |        "label":"OverrideMode",
      |        "type":"choiceproperty",
      |        "typeCustom":"choiceproperty",
      |        "statusModified":false,
      |        "id":"OverrideMode",
      |        "status":"Scheduled",
      |        "canControl":true,
      |        "hasLogging":false,
      |        "hasSchedule":false,
      |        "hasStatus":true,
      |        "isAvailable":true,
      |        "statusLastUpdated":"2020-05-30T05:24:33.477"
      |      },
      |      {
      |        "scheduleChoices":[
      |          {
      |            "label":"AirQualityStatus",
      |            "value":"AirQualityStatus",
      |            "disabled":false,
      |            "moments":null,
      |            "overrideUntil":0
      |          }
      |        ],
      |        "label":"AirQualityStatus",
      |        "type":"choiceproperty",
      |        "typeCustom":"choiceproperty",
      |        "statusModified":false,
      |        "id":"AirQualityStatus",
      |        "status":"Normal",
      |        "canControl":false,
      |        "hasLogging":false,
      |        "hasSchedule":false,
      |        "hasStatus":true,
      |        "isAvailable":true,
      |        "statusLastUpdated":"2020-05-30T05:24:39.377"
      |      },
      |      {
      |        "scheduleChoices":[
      |          {
      |            "label":"Cool",
      |            "value":"Cool",
      |            "disabled":false,
      |            "moments":null,
      |            "overrideUntil":0
      |          },
      |          {
      |            "label":"Heat",
      |            "value":"Heat",
      |            "disabled":false,
      |            "moments":null,
      |            "overrideUntil":0
      |          }
      |        ],
      |        "label":"OperationMode",
      |        "type":"choiceproperty",
      |        "typeCustom":"choiceproperty",
      |        "statusModified":false,
      |        "id":"OperationMode",
      |        "status":"Heat",
      |        "canControl":true,
      |        "hasLogging":false,
      |        "hasSchedule":false,
      |        "hasStatus":true,
      |        "isAvailable":true,
      |        "statusLastUpdated":"2020-06-02T13:56:52.167"
      |      },
      |      {
      |        "label":"OverrideEndTime",
      |        "type":"property",
      |        "typeCustom":"property",
      |        "statusModified":false,
      |        "id":"OverrideEndTime",
      |        "status":null,
      |        "canControl":true,
      |        "hasLogging":false,
      |        "hasSchedule":false,
      |        "hasStatus":true,
      |        "isAvailable":true,
      |        "statusLastUpdated":null
      |      },
      |      {
      |        "scheduleChoices":[
      |          {
      |            "label":"0",
      |            "value":"0",
      |            "disabled":false,
      |            "moments":null,
      |            "overrideUntil":0
      |          }
      |        ],
      |        "max":100.0,
      |        "min":0.0,
      |        "step":0.5,
      |        "label":"AirQuality",
      |        "type":"rangeproperty",
      |        "typeCustom":"rangeproperty",
      |        "statusModified":false,
      |        "id":"AirQuality",
      |        "status":"100.00",
      |        "canControl":false,
      |        "hasLogging":false,
      |        "hasSchedule":false,
      |        "hasStatus":true,
      |        "isAvailable":true,
      |        "statusLastUpdated":"2020-05-30T05:24:39.753"
      |      },
      |      {
      |        "scheduleChoices":[
      |          {
      |            "label":"0",
      |            "value":"0",
      |            "disabled":false,
      |            "moments":null,
      |            "overrideUntil":0
      |          }
      |        ],
      |        "max":100.0,
      |        "min":0.0,
      |        "step":1.0,
      |        "label":"ExhaustFanSpeed",
      |        "type":"rangeproperty",
      |        "typeCustom":"rangeproperty",
      |        "statusModified":false,
      |        "id":"ExhaustFanSpeed",
      |        "status":"0.00",
      |        "canControl":false,
      |        "hasLogging":true,
      |        "hasSchedule":false,
      |        "hasStatus":true,
      |        "isAvailable":true,
      |        "statusLastUpdated":"2020-06-11T17:30:50.273"
      |      },
      |      {
      |        "scheduleChoices":[
      |          {
      |            "label":"0",
      |            "value":"0",
      |            "disabled":false,
      |            "moments":null,
      |            "overrideUntil":0
      |          }
      |        ],
      |        "max":32767.0,
      |        "min":0.0,
      |        "step":1.0,
      |        "label":"CO2",
      |        "type":"rangeproperty",
      |        "typeCustom":"rangeproperty",
      |        "statusModified":false,
      |        "id":"CO2",
      |        "status":"499.00",
      |        "canControl":false,
      |        "hasLogging":true,
      |        "hasSchedule":false,
      |        "hasStatus":true,
      |        "isAvailable":true,
      |        "statusLastUpdated":"2020-06-11T17:50:56.38"
      |      },
      |      {
      |        "scheduleChoices":[
      |          {
      |            "label":"0",
      |            "value":"0",
      |            "disabled":false,
      |            "moments":null,
      |            "overrideUntil":0
      |          }
      |        ],
      |        "max":100.0,
      |        "min":0.0,
      |        "step":1.0,
      |        "label":"ZoneDemand",
      |        "type":"rangeproperty",
      |        "typeCustom":"rangeproperty",
      |        "statusModified":false,
      |        "id":"ZoneDemand",
      |        "status":"0.00",
      |        "canControl":false,
      |        "hasLogging":false,
      |        "hasSchedule":false,
      |        "hasStatus":true,
      |        "isAvailable":true,
      |        "statusLastUpdated":"2020-06-02T13:56:52.137"
      |      },
      |      {
      |        "scheduleChoices":[
      |          {
      |            "label":"-10.00",
      |            "value":"-10.00",
      |            "disabled":false,
      |            "moments":null,
      |            "overrideUntil":0
      |          }
      |        ],
      |        "max":50.0,
      |        "min":-10.0,
      |        "step":0.5,
      |        "label":"Huidige temperatuur",
      |        "type":"rangeproperty",
      |        "typeCustom":"rangeproperty",
      |        "statusModified":false,
      |        "id":"AmbientTemperature",
      |        "status":"21.50",
      |        "canControl":false,
      |        "hasLogging":true,
      |        "hasSchedule":false,
      |        "hasStatus":true,
      |        "isAvailable":true,
      |        "statusLastUpdated":"2020-06-11T17:17:03.177"
      |      },
      |      {
      |        "scheduleChoices":[
      |          {
      |            "label":"Standby",
      |            "value":"16",
      |            "disabled":false,
      |            "moments":null,
      |            "overrideUntil":0
      |          }
      |        ],
      |        "max":30.0,
      |        "min":10.0,
      |        "step":0.5,
      |        "label":"Temperatuur",
      |        "type":"rangeproperty",
      |        "typeCustom":"rangeproperty-temperature",
      |        "statusModified":false,
      |        "id":"SetpointTemperature",
      |        "status":"18.50",
      |        "canControl":true,
      |        "hasLogging":true,
      |        "hasSchedule":true,
      |        "hasStatus":true,
      |        "isAvailable":true,
      |        "statusLastUpdated":"2020-06-11T17:45:41.017"
      |      }
      |    ],
      |    "parameters":{
      |    },
      |    "_etag":"24927608",
      |    "bdrSetting":0
      |  }
      |]
      |""".stripMargin

  private implicit val endpoint: Endpoint = Endpoint("https://test.example.com")
  implicit val testingBackend = SttpBackendStub.synchronous
      .whenRequestMatchesPartial {
        case r if r.method == Method.GET => Response.ok(allDevices)
        case r if r.method == Method.PUT => Response.ok(singleDevice)
      }

  "Devices" should {
    implicit val tokenProvider: TokenProvider = () => Some(TokenResponse("access", "type", 10, "refresh"))
    "be retrieved when a token is present" in {
      val devices = new Devices().retrieveDevices()
      devices.isDefined shouldBe true
      devices.get.size shouldBe 2
    }
    "do an update to check the encoder" in {
      val dev = Device(0, None, UUID.randomUUID(), None, false, "", "", List.empty, Map.empty, 0, 0)
      val device = new nl.kevinvandervlist.ohti.portal.Device().updateDevice(dev)
      device.isDefined shouldBe true
    }
    "Test a full JSON Decoding/Encoding cycle" in {
      import nl.kevinvandervlist.ohti.portal.Devices._
      import io.circe.syntax._
      import io.circe.parser.decode

      decode[List[Device]](allDevices).map(_.asJson) match {
        case Left(e) => fail(e)
        case Right(json) =>
          json.toString().replaceAll("\\s", "") shouldBe allDevices.replaceAll("\\s", "")
      }
    }
  }
}