package nl.kevinvandervlist.ohti.api.model

import java.util.UUID

case class MonitoringData(deviceId: UUID,
                          dataUnit: DataUnit,
                          dateStart: IthoZonedDateTime,
                          interval: Int,
                          timeStamp: IthoZonedDateTime,
                          data: List[Option[BigDecimal]])
