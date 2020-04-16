package nl.kevinvandervlist.ohti.portal

import java.util.UUID

import com.typesafe.scalalogging.LazyLogging
import io.circe
import io.circe.{Decoder, HCursor}
import nl.kevinvandervlist.ohti.api.model.{CubicMeter, DataUnit, IthoZonedDateTime, MonitoringData, Watthour}
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
      }
    }
  }

  case class HackMixedDoubleAndStrings(s: String)

  implicit val decodeMonitoringData: Decoder[MonitoringData] = new Decoder[MonitoringData] {
    final def apply(c: HCursor): Decoder.Result[MonitoringData] = {
      val arr = c.downArray
      for {
        id <- arr.downField("deviceId").as[String]
        unit <- arr.downField("dataUnit").as[DataUnit]
        start <- arr.downField("dateStart").as[Long]
        ts <- arr.downField("timeStamp").as[Long]
        //data <- arr.downField("data").as[List[String]]
        data <- arr.downField("data").as[List[HackMixedDoubleAndStrings]](Decoder.decodeList(dataDecoder))
      } yield {
        //def bd(d: Double): Option[BigDecimal] = if(d.isNaN) { None } else { Some(BigDecimal(d)) }
        def bd(d: HackMixedDoubleAndStrings): Option[BigDecimal] = if(d.s == "NaN") { None } else { Some(BigDecimal(d.s)) }
        MonitoringData(UUID.fromString(id), unit, IthoZonedDateTime.fromTimeStamp(start), IthoZonedDateTime.fromTimeStamp(ts), data.map(bd))
      }
    }
  }

  implicit val dataDecoder: Decoder[HackMixedDoubleAndStrings] =
    Decoder.instance(_.as[String]).or(
      Decoder.instance(_.as[Double].map(_.toString))
    ).map(HackMixedDoubleAndStrings.apply)
}


class Monitoring(private implicit val endpoint: Endpoint,
                 private implicit val tokenProvider: TokenProvider,
                 protected implicit val backend: SttpBackend[Identity, Nothing, NothingT]) extends Client[MonitoringData] {

  def retrieveMonitoringData(interval: Int, uuid: UUID, measurementCount: Int, start: IthoZonedDateTime): Option[MonitoringData] = {
    val request = Util.authorizedRequest(tokenProvider)
      .get(endpoint.monitoring(interval, uuid.toString, measurementCount, start.asTimeStamp))

    val response = request
      .response(asJson[MonitoringData])

    doRequest("Failed to retrieve devices, got code {} - {}", request, response)
  }
}