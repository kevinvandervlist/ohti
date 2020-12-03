package nl.kevinvandervlist.ohti.api.portal

import java.util.UUID

import nl.kevinvandervlist.ohti.portal.{Endpoint, Schedules}
import nl.kevinvandervlist.ohti.portal.TokenManager.{TokenProvider, TokenResponse}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import sttp.client.{Identity, Response}
import sttp.client.testing.SttpBackendStub
import sttp.model.Method

class SchedulesSpec extends AnyWordSpec with Matchers {
  private val simpleSchedule =
    """{
      |  "deviceType":1,
      |  "scheduleId":"af4d7550-0d33-4065-b279-15dec7493976",
      |  "scheduleName":"af4d7550-0d33-4065-b279-15dec7493976",
      |  "scheduledDevices":[
      |    {"id":"af4d7550-0d33-4065-b279-15dec7493976","active":true}
      |  ],
      |  "scheduledProperties":[
      |    {"id":"FanSpeed","scheduleChoices":[
      |      {"label":"-","value":"Away","disabled":false,"moments":[],"overrideUntil":0},
      |      {"label":"1","value":"Low","disabled":false,"moments":null,"overrideUntil":0},
      |      {"label":"2","value":"Medium","disabled":false,"moments":[],"overrideUntil":0},
      |      {"label":"3","value":"High","disabled":false,"moments":[],"overrideUntil":0},
      |      {"label":"A","value":"Auto","disabled":false,"moments":[],"overrideUntil":0}
      |    ]},
      |    {"id":"SetpointTemperature","scheduleChoices":[
      |      {"label":"Standby","value":"18.5","disabled":false,"moments":[
      |        {"day":0,"time":5300000},
      |        {"day":1,"time":12300000},
      |        {"day":2,"time":1000000}
      |      ],"overrideUntil":0},
      |      {"label":"Afwezig","value":"19.5","disabled":false,"moments":[
      |        {"day":3,"time":5300000},
      |        {"day":4,"time":12300000},
      |        {"day":5,"time":1000000}
      |      ],"overrideUntil":0},
      |      {"label":"Aanwezig","value":"19","disabled":false,"moments":[
      |        {"day":6,"time":10000000}
      |      ],"overrideUntil":0},
      |      {"label":"Aangepast1","value":"20","disabled":true,"moments":[],"overrideUntil":0},
      |      {"label":"Aangepast2","value":"20","disabled":true,"moments":[],"overrideUntil":0}
      |    ]}
      |  ],"createdBy":"username@example.com",
      |  "updatedBy":"username@example.com",
      |  "deletedBy":null,
      |  "userId":"username@example.com",
      |  "createdAt":1234567890123,
      |  "updatedAt":1234567898765,
      |  "deletedAt":0,
      |  "id":"af4d7550-0d33-4065-b279-15dec7493976"
      |}""".stripMargin

  private val tempSchedule =
    """{
      |  "deviceType": 1,
      |  "scheduleId": "af4d7550-0d33-4065-b279-15dec7493976",
      |  "scheduleName": "af4d7550-0d33-4065-b279-15dec7493976",
      |  "scheduledDevices": [
      |    {
      |      "id": "af4d7550-0d33-4065-b279-15dec7493976",
      |      "active": true
      |    }
      |  ],
      |  "scheduledProperties": [
      |    {
      |      "id": "FanSpeed",
      |      "scheduleChoices": [
      |        {
      |          "label": "-",
      |          "value": "Away",
      |          "overrideUntil": 0,
      |          "disabled": false,
      |          "moments": []
      |        }
      |      ]
      |    },
      |    {
      |      "id": "SetpointTemperature",
      |      "scheduleChoices": [
      |        {
      |          "label": "Standby",
      |          "value": "19",
      |          "overrideUntil": 0,
      |          "disabled": false,
      |          "moments": [
      |            {
      |              "day": 0,
      |              "time": 2700000
      |            },
      |            {
      |              "day": 2,
      |              "time": 53100000
      |            },
      |            {
      |              "day": 6,
      |              "time": 80100000
      |            }
      |          ]
      |        },
      |        {
      |          "label": "Afwezig",
      |          "value": "20",
      |          "overrideUntil": 0,
      |          "disabled": false,
      |          "moments": [
      |            {
      |              "day": 0,
      |              "time": 18900000
      |            },
      |            {
      |              "day": 5,
      |              "time": 18900000
      |            },
      |            {
      |              "day": 6,
      |              "time": 72900000
      |            }
      |          ]
      |        },
      |        {
      |          "label": "Aanwezig",
      |          "value": "21",
      |          "overrideUntil": 0,
      |          "disabled": false,
      |          "moments": [
      |            {
      |              "day": 0,
      |              "time": 4500000
      |            },
      |            {
      |              "day": 3,
      |              "time": 81900000
      |            },
      |            {
      |              "day": 6,
      |              "time": 81900000
      |            }
      |          ]
      |        },
      |        {
      |          "label": "Aangepast1",
      |          "value": "20",
      |          "overrideUntil": 0,
      |          "disabled": true,
      |          "moments": []
      |        },
      |        {
      |          "label": "Aangepast2",
      |          "value": "20",
      |          "overrideUntil": 0,
      |          "disabled": true,
      |          "moments": []
      |        }
      |      ]
      |    }
      |  ],
      |  "createdBy": "username@example.com",
      |  "updatedBy": "username@example.com",
      |  "deletedBy": null,
      |  "userId": "username@example.com",
      |  "createdAt": 1573155123456,
      |  "updatedAt": 1573155123457,
      |  "deletedAt": 0,
      |  "id": "af4d7550-0d33-4065-b279-15dec7493976"
      |}
      |""".stripMargin

  private implicit val endpoint: Endpoint = Endpoint("https://test.example.com")

  "Schedules" should {
    "be retrieved when a token is present" in {
      implicit val testingBackend: SttpBackendStub[Identity, Nothing, Nothing] = SttpBackendStub.synchronous
        .whenRequestMatchesPartial {
          case r if r.method == Method.GET => Response.ok(simpleSchedule)
        }
      implicit val tokenProvider: TokenProvider = () => Some(TokenResponse("access", "type", 10, "refresh"))
      val schedule = new Schedules().retrieveSchedule(UUID.randomUUID())
      schedule.isDefined shouldBe true
      schedule.get.id shouldBe UUID.fromString("af4d7550-0d33-4065-b279-15dec7493976")
    }
    "encode/decode for a temp schedule" in {
      implicit val testingBackend: SttpBackendStub[Identity, Nothing, Nothing] = SttpBackendStub.synchronous
        .whenRequestMatchesPartial {
          case r if r.method == Method.GET => Response.ok(tempSchedule)
          case r if r.method == Method.PUT => Response.ok(tempSchedule.replaceAll(
            "1573155123457", "1573155123458"
          ))
        }
      implicit val tokenProvider: TokenProvider = () => Some(TokenResponse("access", "type", 10, "refresh"))
      val schedule = new Schedules().retrieveSchedule(UUID.randomUUID())
      schedule.isDefined shouldBe true
      val updated = new Schedules().updateSchedule(schedule.get)
      updated.isDefined shouldBe true

      // updated should be changed
      schedule.get.updatedAt should not be updated.get.updatedAt
      // the rest should be identical
      updated.map(_.copy(updatedAt = schedule.get.updatedAt)) shouldBe schedule
    }
  }
}


