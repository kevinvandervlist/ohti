package nl.kevinvandervlist.ohti.api.model

import java.util.UUID

case class Zone(id: UUID, components: List[ZoneComponent], isOnline: Boolean, name: String)

case class ZoneComponent(id: UUID, address: String, `type`: ZoneComponentType, deleted: Boolean)

sealed trait ZoneComponentType

case object Thermostat extends ZoneComponentType
case object Fan extends ZoneComponentType
