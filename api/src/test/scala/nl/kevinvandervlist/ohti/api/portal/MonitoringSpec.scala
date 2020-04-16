package nl.kevinvandervlist.ohti.api.portal

import java.util.UUID

import nl.kevinvandervlist.ohti.api.model.IthoZonedDateTime
import nl.kevinvandervlist.ohti.portal.{Endpoint, Monitoring}
import nl.kevinvandervlist.ohti.portal.TokenManager._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import sttp.client.Response
import sttp.client.testing.SttpBackendStub
import sttp.model.Method

class MonitoringSpec extends AnyWordSpec with Matchers {
  val response =
    """[
      |  {
      |    "id":"11.45fba720-c04a-4b6c-a471-e9a5d5c0d3c4.10.bin",
      |    "deviceId":"45fba720-c04a-4b6c-a471-e9a5d5c0d3c4",
      |    "category":11,
      |    "dataUnit":10,
      |    "dateStart":1583054800000,
      |    "dataGrouping":1440,
      |    "dataLength":2,
      |    "data":[100.0,23.0,"NaN"],
      |    "totalEnergy":{"normal":123.0,"low":45.0},
      |    "timeStamp":1583056800000,
      |    "eTag":"\"0xDEADBEEFDEADBEE\"",
      |    "notifyChannel":"11.45fba720-c04a-4b6c-a471-e9a5d5c0d3c4.10.bin"
      |  }
      |]""".stripMargin

  private implicit val endpoint: Endpoint = Endpoint("https://test.example.com")
  implicit val testingBackend = SttpBackendStub.synchronous
      .whenRequestMatchesPartial {
        case r if r.method == Method.GET => Response.ok(response)
      }

  "Monitoring data" should {
    "be retrieved when a token is present" in {
      implicit val tokenProvider: TokenProvider = () => Some(TokenResponse("access", "type", 10, "refresh"))
      val monitoring = new Monitoring().retrieveMonitoringData(1440, UUID.fromString("5fba720-c04a-4b6c-a471-e9a5d5c0d3c4"), 2, IthoZonedDateTime.fromTimeStamp(1583054800000L))
      monitoring.isDefined shouldBe true
    }
    "not be retrieved without a token" in {
      implicit val tokenProvider: TokenProvider = () => None
      assertThrows[IllegalStateException] {
        new Monitoring().retrieveMonitoringData(1440, UUID.fromString("5fba720-c04a-4b6c-a471-e9a5d5c0d3c4"), 2, IthoZonedDateTime.fromTimeStamp(1583054800000L))
      }
    }
  }
}
