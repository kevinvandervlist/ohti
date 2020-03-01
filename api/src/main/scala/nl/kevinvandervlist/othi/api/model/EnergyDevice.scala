package nl.kevinvandervlist.othi.api.model

import java.time.ZonedDateTime
import java.util.UUID

case class EnergyDevice(id: UUID, name: String, value: BigDecimal, time: ZonedDateTime, energyType: EnergyType)