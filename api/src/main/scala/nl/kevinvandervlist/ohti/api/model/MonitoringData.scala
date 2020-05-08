package nl.kevinvandervlist.ohti.api.model

import java.util.UUID

case class MonitoringData(deviceId: UUID,
                          category: Category,
                          dataUnit: DataUnit,
                          dateStart: IthoZonedDateTime,
                          interval: Int,
                          timeStamp: IthoZonedDateTime,
                          data: List[Option[BigDecimal]])
