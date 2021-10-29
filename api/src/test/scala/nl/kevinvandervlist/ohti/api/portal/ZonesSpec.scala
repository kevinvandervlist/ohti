package nl.kevinvandervlist.ohti.api.portal

import java.util.UUID

import nl.kevinvandervlist.ohti.portal.TokenManager._
import nl.kevinvandervlist.ohti.portal.{Endpoint, Zones}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import sttp.client3.Response
import sttp.client3.testing.SttpBackendStub
import sttp.model.Method

class ZonesSpec extends AnyWordSpec with Matchers {
  private val zones =
    """[
      |  {"id":"3e3285c2-a3dc-4be6-b585-184c4733e384","masterZoneId":null,"zoneComponents":[],"isOnline":false,"isModified":false,"name":"Niet toegewezen componenten"},
      |  {"id":"3e3285c2-a3dc-4be6-b585-184c4733e385","masterZoneId":null,"zoneComponents":[{"id":"3e3285c2-a3dc-4be6-b585-184c4733e387","address":"12A34B","type":"Thermostaat","deleted":false}],"isOnline":true,"isModified":false,"name":"Upstairs"},
      |  {"id":"3e3285c2-a3dc-4be6-b585-184c4733e386","masterZoneId":null,"zoneComponents":[{"id":"3e3285c2-a3dc-4be6-b585-184c4733e388","address":"12A34C","type":"Thermostaat","deleted":false},{"id":"3e3285c2-a3dc-4be6-b585-184c4733e389","address":"12A34C","type":"Ventilator","deleted":false}],"isOnline":true,"isModified":false,"name":"Downstairs"}
      |]""".stripMargin

  private implicit val endpoint: Endpoint = Endpoint("https://test.example.com")
  implicit val testingBackend = SttpBackendStub.synchronous
      .whenRequestMatchesPartial {
        case r if r.method == Method.GET => Response.ok(zones)
      }

  "Zones devices" should {
    "be retrieved when a token is present" in {
      implicit val tokenProvider: TokenProvider = () => Some(TokenResponse("access", "type", 10, "refresh"))
      val zones = new Zones().retrieveZones()
      zones.isDefined shouldBe true
      zones.get.size shouldBe 3
      zones.get.flatMap(_.components).size shouldBe 3
    }
    "not be retrieved without a token" in {
      implicit val tokenProvider: TokenProvider = () => None
      assertThrows[IllegalStateException] {
        new Zones().retrieveZones()
      }
    }
  }
  "Zones conversion" should {
    implicit val tokenProvider: TokenProvider = () => Some(TokenResponse("access", "type", 10, "refresh"))
    val zones = new Zones().retrieveZones().get
    "retrieve the fans" in {
      val fanZones = zones.fans
      fanZones.size shouldBe 1
      fanZones.exists(_.id == UUID.fromString("3e3285c2-a3dc-4be6-b585-184c4733e386"))
      val fans = fanZones.components
      fans.size shouldBe 1
      fans.exists(_.id == UUID.fromString("3e3285c2-a3dc-4be6-b585-184c4733e389"))
    }
    "retrieve the central electric consumption meter(s)" in {
      val thermostatZones = zones.thermostats
      thermostatZones.size shouldBe 2
      thermostatZones.exists(_.id == UUID.fromString("3e3285c2-a3dc-4be6-b585-184c4733e385"))
      thermostatZones.exists(_.id == UUID.fromString("3e3285c2-a3dc-4be6-b585-184c4733e386"))

      val thermostats = thermostatZones.components
      thermostats.size shouldBe 2
      thermostats.exists(_.id == UUID.fromString("3e3285c2-a3dc-4be6-b585-184c4733e387"))
      thermostats.exists(_.id == UUID.fromString("3e3285c2-a3dc-4be6-b585-184c4733e388"))
    }
  }
}
