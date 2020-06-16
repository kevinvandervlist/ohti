package nl.kevinvandervlist.ohti.api.operations

import nl.kevinvandervlist.ohti.api.model.{DeviceProperty, IthoZonedDateTime}
import nl.kevinvandervlist.ohti.api.operations

import scala.language.implicitConversions

object DevicePropertyOps {
  implicit def richDeviceProperty(prop: DeviceProperty): DevicePropertyOps = new DevicePropertyOps(prop)
  implicit def plainDeviceProperty(prop: DevicePropertyOps): DeviceProperty = prop.property

  private val heat = "Heat"
  private val cool = "Cool"
}

class DevicePropertyOps(val property: DeviceProperty) {
  def hasCoolHeatChoice: Boolean = hasLabels(DevicePropertyOps.heat, DevicePropertyOps.cool)

  def hasFanSpeedControl: Boolean =
    property.id == "FanSpeed"

  def isCooling: Boolean = hasStatus(DevicePropertyOps.cool)
  def isHeating: Boolean = hasStatus(DevicePropertyOps.heat)

  def setCoolChoice(): Option[DevicePropertyOps] =
    setStatus(DevicePropertyOps.cool)

  def setHeatChoice(): Option[DevicePropertyOps] =
    setStatus(DevicePropertyOps.heat)

  def hasLabels(labels: String*): Boolean = property.scheduleChoices match {
    case None => false
    case Some(choices) => labels.forall(label => choices.exists(_.label == label))
  }

  private def hasStatus(status: String): Boolean = property.status.contains(status)

  private def setStatus(choice: String): Option[DevicePropertyOps] = {
    if(hasCoolHeatChoice) {
      Some(new operations.DevicePropertyOps(
        property.copy(
          status = Some(choice),
          statusModified = true,
          statusLastUpdated = Some(IthoZonedDateTime.today.asPortalString + ".00")
        )))
    } else {
      None
    }
  }
}