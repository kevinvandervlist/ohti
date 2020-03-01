package nl.kevinvandervlist.othi.api.model

sealed trait EnergyType

case object Gas extends EnergyType
case object Electricity extends EnergyType