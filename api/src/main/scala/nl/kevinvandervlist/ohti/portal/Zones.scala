package nl.kevinvandervlist.ohti.portal

import java.util.UUID

import io.circe.{Decoder, HCursor}
import nl.kevinvandervlist.ohti.api.model.{Fan, Thermostat, Zone, ZoneComponent, ZoneComponentType}
import nl.kevinvandervlist.ohti.portal.Zones._
import nl.kevinvandervlist.ohti.portal.TokenManager._
import sttp.client._
import sttp.client.circe._

object Zones {
  implicit val decodeZone: Decoder[Zone] = new Decoder[Zone] {
    final def apply(c: HCursor): Decoder.Result[Zone] =
      for {
        id <- c.downField("id").as[String]
        components <- c.downField("zoneComponents").as[List[ZoneComponent]]
        isOnline <- c.downField("isOnline").as[Boolean]
        name <- c.downField("name").as[String]
      } yield {
        Zone(UUID.fromString(id), components, isOnline, name)
      }
  }

  implicit val decodeZoneComponent: Decoder[ZoneComponent] = new Decoder[ZoneComponent] {
    final def apply(c: HCursor): Decoder.Result[ZoneComponent] =
      for {
        id <- c.downField("id").as[String]
        addr <- c.downField("address").as[String]
        tpe <- c.downField("type").as[String]
        deleted <- c.downField("deleted").as[Boolean]
      } yield {
        ZoneComponent(UUID.fromString(id), addr, asZoneComponentType(tpe), deleted)
      }
  }

  private def asZoneComponentType(`type`: String): ZoneComponentType = `type` match {
    case "Thermostaat" => Thermostat
    case "Ventilator" => Fan
    case otherwise => throw new NotImplementedError(s"Unknown zone component type: $otherwise")
  }
}

class Zones(private implicit val endpoint: Endpoint,
            private implicit val tokenProvider: TokenProvider,
            protected implicit val backend: SttpBackend[Identity, Nothing, NothingT]) extends Client[List[Zone]] {

  def retrieveZones(): Option[List[Zone]] = {
    val request = Util.authorizedRequest(tokenProvider)
      .get(endpoint.zones)

    val response = request
      .response(asJson[List[Zone]])

    doRequest("Failed to retrieve zones, got code {} - {}", response)
  }
}
