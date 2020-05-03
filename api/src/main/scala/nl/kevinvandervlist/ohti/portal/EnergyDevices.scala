package nl.kevinvandervlist.ohti.portal

import java.util.UUID

import io.circe.{Decoder, HCursor}
import nl.kevinvandervlist.ohti.api.model.{Electricity, EnergyDevice, EnergyType, Gas, IthoZonedDateTime}
import nl.kevinvandervlist.ohti.portal.EnergyDevices._
import nl.kevinvandervlist.ohti.portal.TokenManager._
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
        isOnline <- c.downField("isOnline").as[Boolean]
        isCentralMeter <- c.downField("isCentralMeter").as[Boolean]
        isProducer <- c.downField("isProducer").as[Boolean]
        meterValue <- c.downField("meterIndexValue").as[Option[BigDecimal]]
        et <- c.downField("energyType").as[Int]
      } yield {
        // Note: I _think_ we are always in CET here
        val zdt = ots.map(IthoZonedDateTime.fromPortalString)
        val v = if(value.isNaN) { None } else { Some(BigDecimal(value)) }
        EnergyDevice(UUID.fromString(id), name, v, zdt, asEnergyType(et), isOnline, isCentralMeter, isProducer, meterValue)
      }
  }

  private def asEnergyType(code: Int): EnergyType = code match {
    case 10 => Electricity
    case 20 => Gas
    case otherwise => throw new NotImplementedError(s"Unknown energy type: $otherwise")
  }

  implicit class RichEnergyDevices(val devices: List[EnergyDevice]) {
    def gasMeters: List[EnergyDevice] = devices
      .filter(_.energyType == Gas)
      .filter(_.isOnline)

    def electricCentralMeterConsumption: List[EnergyDevice] = devices
      .filter(_.isCentralMeter)
      .filterNot(_.isProducer)
      .filter(_.isOnline)
      .filter(_.energyType == Electricity)

    def electricCentralMeterFeedback: List[EnergyDevice] = devices
      .filter(_.isCentralMeter)
      .filter(_.isProducer)
      .filter(_.isOnline)
      .filter(_.energyType == Electricity)

    def electricProduction: List[EnergyDevice] = devices
      .filterNot(_.isCentralMeter)
      .filter(_.isProducer)
      .filter(_.isOnline)
      .filter(_.energyType == Electricity)
  }
}

class EnergyDevices(private implicit val endpoint: Endpoint,
                    private implicit val tokenProvider: TokenProvider,
                    protected implicit val backend: SttpBackend[Identity, Nothing, NothingT]) extends Client[List[EnergyDevice]] {

  def retrieveDevices(): Option[List[EnergyDevice]] = {
    val request = Util.authorizedRequest(tokenProvider)
      .get(endpoint.energyDevices)

    val response = request
      .response(asJson[List[EnergyDevice]])

    doRequest("Failed to retrieve devices, got code {} - {}", response)
  }
}
