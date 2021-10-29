package nl.kevinvandervlist.ohti.api.portal

import java.util.UUID

import nl.kevinvandervlist.ohti.api.model.IthoZonedDateTime
import nl.kevinvandervlist.ohti.portal.{Endpoint, Monitoring}
import nl.kevinvandervlist.ohti.portal.TokenManager._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import sttp.client3.{Identity, Response}
import sttp.client3.testing.SttpBackendStub
import sttp.model.Method

class MonitoringSpec extends AnyWordSpec with Matchers {
  private implicit val endpoint: Endpoint = Endpoint("https://test.example.com")
  private def backend(response: String) =
    SttpBackendStub.synchronous
      .whenRequestMatchesPartial {
        case r if r.method == Method.GET => Response.ok(response)
      }

  "power measurements" should {
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
    implicit val testingBackend: SttpBackendStub[Identity, Any] = backend(response)
    "be retrieved when a token is present" in {
      implicit val tokenProvider: TokenProvider = () => Some(TokenResponse("access", "type", 10, "refresh"))
      val monitoring = new Monitoring().retrieveMonitoringData(1440, UUID.fromString("5fba720-c04a-4b6c-a471-e9a5d5c0d3c4"), 2, IthoZonedDateTime.fromTimeStamp(1583054800000L))
      monitoring.isDefined shouldBe true
      monitoring.get.size shouldBe 1
    }
    "not be retrieved without a token" in {
      implicit val tokenProvider: TokenProvider = () => None
      assertThrows[IllegalStateException] {
        new Monitoring().retrieveMonitoringData(1440, UUID.fromString("5fba720-c04a-4b6c-a471-e9a5d5c0d3c4"), 2, IthoZonedDateTime.fromTimeStamp(1583054800000L))
      }
    }
  }
  "temperature, fan and CO2 measurements" should {
    val response =
      """[
        |  {
        |    "id":"100.ff0819b2-4a8e-47be-a0ae-78a86280948a.30.bin",
        |    "deviceId":"ff0819b2-4a8e-47be-a0ae-78a86280948a",
        |    "category":100,
        |    "dataUnit":30,
        |    "dateStart":1588012300000,
        |    "dataGrouping":15,
        |    "dataLength":3,
        |    "data":[21.0,21.5,"NaN"],
        |    "totalEnergy":{"normal":0.0,"low":0.0},
        |    "timeStamp":1588012300000,
        |    "eTag":"\"0xDEADBEEF0001021\"",
        |    "notifyChannel":"100.ff0819b2-4a8e-47be-a0ae-78a86280948a.30.bin"
        |  },{
        |    "id":"110.ff0819b2-4a8e-47be-a0ae-78a86280948a.30.bin",
        |    "deviceId":"ff0819b2-4a8e-47be-a0ae-78a86280948a",
        |    "category":110,
        |    "dataUnit":30,
        |    "dateStart":1588012300000,
        |    "dataGrouping":15,
        |    "dataLength":2,
        |    "data":[19.0,"NaN"],
        |    "totalEnergy":{"normal":0.0,"low":0.0},
        |    "timeStamp":1588012300000,
        |    "eTag":"\"0xDEADBEEF0001022\"",
        |    "notifyChannel":"110.ff0819b2-4a8e-47be-a0ae-78a86280948a.30.bin"
        |  },{
        |    "id":"70.ff0819b2-4a8e-47be-a0ae-78a86280948a.60.bin",
        |    "deviceId":"ff0819b2-4a8e-47be-a0ae-78a86280948a",
        |    "category":70,
        |    "dataUnit":60,
        |    "dateStart":1588012300000,
        |    "dataGrouping":15,
        |    "dataLength":4,
        |    "data":[756.0,760.0,750.0,"NaN"],
        |    "totalEnergy":{"normal":0.0,"low":0.0},
        |    "timeStamp":1588012300000,
        |    "eTag":"\"0xDEADBEEF0001023\"",
        |    "notifyChannel":"70.ff0819b2-4a8e-47be-a0ae-78a86280948a.60.bin"
        |  },{
        |    "id":"80.ff0819b2-4a8e-47be-a0ae-78a86280948a.50.bin",
        |    "deviceId":"ff0819b2-4a8e-47be-a0ae-78a86280948a",
        |    "category":80,
        |    "dataUnit":50,
        |    "dateStart":1588012300000,
        |    "dataGrouping":15,
        |    "dataLength":3,
        |    "data":[36.0,35.0,"NaN"],
        |    "totalEnergy":{"normal":0.0,"low":0.0},
        |    "timeStamp":1588012300000,
        |    "eTag":"\"0xDEADBEEF0001024\"",
        |    "notifyChannel":"80.ff0819b2-4a8e-47be-a0ae-78a86280948a.50.bin"
        |  }
        |]""".stripMargin
    implicit val testingBackend: SttpBackendStub[Identity, Any] = backend(response)
    "be retrieved when a token is present" in {
      implicit val tokenProvider: TokenProvider = () => Some(TokenResponse("access", "type", 10, "refresh"))
      val monitoring = new Monitoring().retrieveMonitoringData(15, UUID.fromString("ff0819b2-4a8e-47be-a0ae-78a86280948a"), 2, IthoZonedDateTime.fromTimeStamp(1588012300000L))
      monitoring.isDefined shouldBe true
      monitoring.get.size shouldBe 4
    }
  }
}
