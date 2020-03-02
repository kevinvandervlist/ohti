package nl.kevinvandervlist.othi.portal

import java.util.UUID

import com.typesafe.scalalogging.LazyLogging
import io.circe.{Decoder, HCursor}
import nl.kevinvandervlist.othi.api.model.{CubicMeter, DataUnit, IthoZonedDateTime, MonitoringData, Watthour}
import nl.kevinvandervlist.othi.portal.TokenManager._
import nl.kevinvandervlist.othi.portal.Monitoring._
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

  implicit val decodeMonitoringData: Decoder[MonitoringData] = new Decoder[MonitoringData] {
    final def apply(c: HCursor): Decoder.Result[MonitoringData] = {
      val arr = c.downArray
      for {
        id <- arr.downField("deviceId").as[String]
        unit <- arr.downField("dataUnit").as[DataUnit]
        start <- arr.downField("dateStart").as[Long]
        ts <- arr.downField("timeStamp").as[Long]
        data <- arr.downField("data").as[List[Double]]
      } yield {
        def bd(d: Double): Option[BigDecimal] = if(d.isNaN) { None } else { Some(BigDecimal(d)) }
        MonitoringData(UUID.fromString(id), unit, IthoZonedDateTime.fromTimeStamp(start), IthoZonedDateTime.fromTimeStamp(ts), data.map(bd))
      }
    }
  }
}


class Monitoring(private implicit val tokenProvider: TokenProvider,
                    private implicit val backend: SttpBackend[Identity, Nothing, NothingT]) extends LazyLogging {
  private def uri(interval: Int, uuid: UUID, measurementCount: Int, start: IthoZonedDateTime) = {
    uri"https://mijn.ithodaalderop.nl/api/monitoring/$interval/devices/${uuid.toString}/?take=$measurementCount&start=${start.asTimeStamp}"
  }

  def retrieveMonitoringData(interval: Int, uuid: UUID, measurementCount: Int, start: IthoZonedDateTime): Option[MonitoringData] = {
    val request = Util.authorizedRequest(tokenProvider)
      .get(uri(interval, uuid, measurementCount, start))

    val response = request
      .response(asJson[MonitoringData])
      .send()

    if(response.code.isSuccess) {
      response.body.toOption
    } else {
      logger.error("Failed to retrieve devices, got code {} - {}", response.code, response.body.toString)
      None
    }
  }
}
