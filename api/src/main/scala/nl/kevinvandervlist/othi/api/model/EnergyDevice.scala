package nl.kevinvandervlist.othi.api.model

import java.time.ZonedDateTime
import java.util.UUID

case class EnergyDevice(id: UUID, name: String, value: Option[BigDecimal], time: Option[ZonedDateTime], energyType: EnergyType)