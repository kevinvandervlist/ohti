package nl.kevinvandervlist.ohti.api.model

import java.util.UUID

case class EnergyDevice(id: UUID, name: String, value: Option[BigDecimal], time: Option[IthoZonedDateTime], energyType: EnergyType)