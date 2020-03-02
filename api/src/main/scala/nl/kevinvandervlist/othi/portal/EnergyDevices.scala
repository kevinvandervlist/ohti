package nl.kevinvandervlist.othi.portal

import java.time.ZonedDateTime
import java.util.UUID

import com.typesafe.scalalogging.LazyLogging
import io.circe.{Decoder, HCursor}
import nl.kevinvandervlist.othi.api.model.{ElectricityLowTariff, ElectricityNormalTariff, EnergyDevice, EnergyType, Gas}
import nl.kevinvandervlist.othi.portal.EnergyDevices._
import nl.kevinvandervlist.othi.portal.TokenManager._
import sttp.client._
import sttp.client.circe._

object EnergyDevices {
  implicit val decodeEnergyDevice: Decoder[EnergyDevice] = new Decoder[EnergyDevice] {
    final def apply(c: HCursor): Decoder.Result[EnergyDevice] =
      for {
        id <- c.downField("id").as[String]
        name <- c.downField("name").as[String]
        value <- c.downField("meterIndexCurrentValue").as[Double]
        ots <- c.downField("meterIndexTimestamp").as[Option[String]]
        et <- c.downField("energyType").as[Int]
      } yield {
        // Note: I _think_ we are always in CET here
        val zdt = ots.map(ts => ZonedDateTime.parse(s"$ts+01:00"))
        val v = if(value.isNaN) { None } else { Some(BigDecimal(value)) }
        EnergyDevice(UUID.fromString(id), name, v, zdt, asEnergyType(et))
      }
  }

  private def asEnergyType(code: Int): EnergyType = code match {
    case 10 => ElectricityLowTariff
    case 20 => Gas
    case 200 => ElectricityNormalTariff
    case otherwise => throw new NotImplementedError(s"Unknown energy type: $otherwise")
  }
}

class EnergyDevices(private implicit val tokenProvider: TokenProvider,
                    private implicit val backend: SttpBackend[Identity, Nothing, NothingT]) extends LazyLogging {
  private val uri = uri"https://mijn.ithodaalderop.nl/api/devices/energy/energyDevices"

  def retrieveDevices(): Option[List[EnergyDevice]] = {
    val request = Util.authorizedRequest(tokenProvider)
      .get(uri)

    val response = request
      .response(asJson[List[EnergyDevice]])
      .send()

    if(response.code.isSuccess) {
      response.body.toOption
    } else {
      logger.error("Failed to retrieve devices, got code {} - {}", response.code, response.body.toString)
      None
    }
  }
}
