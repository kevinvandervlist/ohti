package nl.kevinvandervlist.ohti.api.model

import java.util.UUID
import scala.language.implicitConversions

case class Zone(id: UUID, components: List[ZoneComponent], isOnline: Boolean, name: String)

case class ZoneComponent(id: UUID, address: String, `type`: ZoneComponentType, deleted: Boolean)

sealed trait ZoneComponentType

case object Thermostat extends ZoneComponentType
case object Fan extends ZoneComponentType

object Zones {
  implicit def toList(container: Zones): List[Zone] = container.devices
  implicit def toContainer(list: List[Zone]): Zones = Zones(list)
}

case class Zones(devices: List[Zone]) {
  @inline
  private def fans(components: List[ZoneComponent]): List[ZoneComponent] =
    components.filter(_.`type` == Fan)

  @inline
  private def thermostats(components: List[ZoneComponent]): List[ZoneComponent] =
    components.filter(_.`type` == Thermostat)

  @inline
  private def filter(f: List[ZoneComponent] => List[ZoneComponent]): PartialFunction[Zone, Zone] = {
    case z: Zone if f(z.components).nonEmpty => z.copy(components = f(z.components))
  }

  def thermostats: Zones = devices.collect(filter(thermostats))
  def fans: Zones = devices.collect(filter(fans))
  def components: List[ZoneComponent] = this.flatMap(_.components)
}