package nl.kevinvandervlist.ohti.api.model

sealed trait EnergyType

case object Gas extends EnergyType
case object ElectricityLowTariff extends EnergyType
case object ElectricityNormalTariff extends EnergyType