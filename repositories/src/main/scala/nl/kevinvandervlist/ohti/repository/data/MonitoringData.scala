package nl.kevinvandervlist.ohti.repository.data

import java.time.temporal.ChronoUnit
import java.util.UUID

import nl.kevinvandervlist.ohti.api.model.MonitoringData

import scala.language.implicitConversions

object MonitoringDataValue {
  implicit def toValues(md: MonitoringData): List[MonitoringDataValue] = md.data.map {
      case None => null
      case Some(bd: BigDecimal) => bd
    }.zipWithIndex.map {
      case (v, i) =>
        val s = md.dateStart.add(i * md.interval, ChronoUnit.MINUTES)
        val e = s.add(md.interval, ChronoUnit.MINUTES)
        val mdi = MonitoringDataIndex(md.deviceId, s.asTimeStamp, e.asTimeStamp)
        MonitoringDataValue(mdi, v, md.dataUnit.toString)
    }
}

case class MonitoringDataIndex(deviceUUID: UUID,
                               from: Long,
                               to: Long)

case class MonitoringDataValue(index: MonitoringDataIndex, value: BigDecimal, unit: String)