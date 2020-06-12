package nl.kevinvandervlist.ohti.api.portal

import java.util.UUID

import nl.kevinvandervlist.ohti.portal.{Endpoint, Schedules}
import nl.kevinvandervlist.ohti.portal.TokenManager.{TokenProvider, TokenResponse}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import sttp.client.Response
import sttp.client.testing.SttpBackendStub
import sttp.model.Method

class SchedulesSpec extends AnyWordSpec with Matchers {
  private val schedule =
    """{
      |  "deviceType":1,
      |  "scheduleId":"45fba720-c04a-4b6c-a471-e9a5d5c0d3c4",
      |  "scheduleName":"45fba720-c04a-4b6c-a471-e9a5d5c0d3c4",
      |  "scheduledDevices":[
      |    {"id":"45fba720-c04a-4b6c-a471-e9a5d5c0d3c4","active":true}
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
      |  "id":"45fba720-c04a-4b6c-a471-e9a5d5c0d3c4"
      |}""".stripMargin

  private implicit val endpoint: Endpoint = Endpoint("https://test.example.com")
  implicit val testingBackend = SttpBackendStub.synchronous
    .whenRequestMatchesPartial {
      case r if r.method == Method.GET => Response.ok(schedule)
    }

  "Schedules" should {
    "be retrieved when a token is present" in {
      implicit val tokenProvider: TokenProvider = () => Some(TokenResponse("access", "type", 10, "refresh"))
      val schedule = new Schedules().retrieveSchedule(UUID.randomUUID())
      schedule.isDefined shouldBe true
      schedule.get.id shouldBe UUID.fromString("45fba720-c04a-4b6c-a471-e9a5d5c0d3c4")
    }
  }
}


