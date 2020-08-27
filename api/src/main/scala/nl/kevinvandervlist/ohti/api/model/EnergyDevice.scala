package nl.kevinvandervlist.ohti.api.model

import java.util.UUID
import scala.language.implicitConversions

case class EnergyDevice(id: UUID,
                        name: String,
                        value: Option[BigDecimal],
                        time: Option[IthoZonedDateTime],
                        energyType: EnergyType,
                        isOnline: Boolean,
                        isCentralMeter: Boolean,
                        isProducer: Boolean,
                        meterValue: Option[BigDecimal],
                        energyDeviceId: UUID)
object EnergyDevices {
  implicit def toList(container: EnergyDevices): List[EnergyDevice] = container.devices
  implicit def toContainer(list: List[EnergyDevice]): EnergyDevices = EnergyDevices(list)
}

case class EnergyDevices(devices: List[EnergyDevice]) {
  def gasMeters: EnergyDevices = devices
    .filter(_.energyType == Gas)
    .filter(_.isOnline)

  def electricCentralMeterConsumption: EnergyDevices = devices
    .filter(_.isCentralMeter)
    .filterNot(_.isProducer)
    .filter(_.isOnline)
    .filter(_.energyType == Electricity)

  def electricCentralMeterFeedback: EnergyDevices = devices
    .filter(_.isCentralMeter)
    .filter(_.isProducer)
    .filter(_.isOnline)
    .filter(_.energyType == Electricity)

  def electricProduction: EnergyDevices = devices
    .filterNot(_.isCentralMeter)
    .filter(_.isProducer)
    .filter(_.isOnline)
    .filter(_.energyType == Electricity)
}