package nl.kevinvandervlist.ohti.portal

import java.util.UUID

import io.circe.{Decoder, HCursor}
import nl.kevinvandervlist.ohti.api.model._
import nl.kevinvandervlist.ohti.portal.TokenManager._
import nl.kevinvandervlist.ohti.portal.Monitoring._
import sttp.client._
import sttp.client.circe._

object Monitoring {
  implicit val decodeDataUnit: Decoder[DataUnit] = new Decoder[DataUnit] {
    final def apply(c: HCursor): Decoder.Result[DataUnit] = for {
      u <- c.as[Int]
    } yield {
      u match {
        case 10 => Watthour
        case 20 => CubicMeter
        case 30 => Celcius
        case 60 => CO2
        case 50 => FanSpeed
      }
    }
  }

  implicit val decodeCategory: Decoder[Category] = new Decoder[Category] {
    final def apply(c: HCursor): Decoder.Result[Category] = for {
      u <- c.as[Int]
    } yield {
      u match {
        case 100 => ActualTemperatureCelcius
        case 110 => ConfiguredTemperatureCelcius
        case 70 => PartsPerMillion
        case 80 => FanSpeedPercentage
        case 20 => CentralMeterGasUsage
        case 12 => ElectricityProduction
        case 11 => ElectricityUsage
      }
    }
  }

  case class HackMixedDoubleAndStrings(s: String)

  implicit val decodeMonitoringData: Decoder[MonitoringData] = new Decoder[MonitoringData] {
    final def apply(c: HCursor): Decoder.Result[MonitoringData] = for {
      id <- c.downField("deviceId").as[String]
      category <- c.downField("category").as[Category]
      unit <- c.downField("dataUnit").as[DataUnit]
      start <- c.downField("dateStart").as[Long]
      ts <- c.downField("timeStamp").as[Long]
      interval <- c.downField("dataGrouping").as[Int]
      data <- c.downField("data").as[List[HackMixedDoubleAndStrings]](Decoder.decodeList(dataDecoder))
    } yield {
      def bd(d: HackMixedDoubleAndStrings): Option[BigDecimal] = if(d.s == "NaN") { None } else { Some(BigDecimal(d.s)) }
      MonitoringData(UUID.fromString(id), category, unit, IthoZonedDateTime.fromTimeStamp(start), interval, IthoZonedDateTime.fromTimeStamp(ts), data.map(bd))
    }
  }

  implicit val dataDecoder: Decoder[HackMixedDoubleAndStrings] =
    Decoder.instance(_.as[String]).or(
      Decoder.instance(_.as[Double].map(_.toString))
    ).map(HackMixedDoubleAndStrings.apply)
}


class Monitoring(private implicit val endpoint: Endpoint,
                 private implicit val tokenProvider: TokenProvider,
                 protected implicit val backend: SttpBackend[Identity, Nothing, NothingT]) extends Client[List[MonitoringData]] {

  def retrieveMonitoringData(interval: Int, uuid: UUID, measurementCount: Int, start: IthoZonedDateTime): Option[List[MonitoringData]] = {
    val request = Util.authorizedRequest(tokenProvider)
      .get(endpoint.monitoring(interval, uuid.toString, measurementCount, start.asTimeStamp))

    val response = request
      .response(asJson[List[MonitoringData]])

    doRequest("Failed to retrieve devices, got code {} - {}", response)
  }
}