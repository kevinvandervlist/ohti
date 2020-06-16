package nl.kevinvandervlist.ohti.api.operations

import nl.kevinvandervlist.ohti.api.model.Device
import nl.kevinvandervlist.ohti.api.operations.DevicePropertyOps._

import scala.language.implicitConversions

object DeviceOps {
  implicit def richDevice(dev: Device): DeviceOps = new DeviceOps(dev)
  implicit def plainDevice(dev: DeviceOps): Device = dev.device
}

class DeviceOps(val device: Device) {
  def hasCoolHeatChoice: Boolean =
    device.properties.exists(p => p.hasCoolHeatChoice)

  def hasFanSpeedControl: Boolean =
    device.properties.exists(p => p.hasFanSpeedControl)

  def setHeatChoice(): Option[DeviceOps] = if(hasCoolHeatChoice) {
    setChoice(
      prop => prop.hasCoolHeatChoice && prop.isCooling,
      prop => prop.setHeatChoice(),
    )
  } else {
    None
  }

  def setCoolChoice(): Option[DeviceOps] = if(hasCoolHeatChoice) {
    setChoice(
      prop => prop.hasCoolHeatChoice && prop.isHeating,
      prop => prop.setCoolChoice(),
    )
  } else {
    None
  }

  private def setChoice(pred: DevicePropertyOps => Boolean,
                        set: DevicePropertyOps => Option[DevicePropertyOps]): Option[DeviceOps] = {
    val updatedProperties: List[Option[DevicePropertyOps]] = device.properties.map {
      case p if pred(p) => set(p)
      case otherwise => Some(otherwise)
    }
    if (updatedProperties.exists(_.isEmpty)) {
      return None
    }
    val validatedProperties = updatedProperties.map(_.get).map(_.property)
    // If nothing changed, this should be None
    if(validatedProperties == device.properties) {
      return None
    }
    // Otherwise the properties can be updated
    Some(new DeviceOps(device.copy(
      properties = updatedProperties.map(_.get),
      _etag = device._etag + 1
    )))
  }
}