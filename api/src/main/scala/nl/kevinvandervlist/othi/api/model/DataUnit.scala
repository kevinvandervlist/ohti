package nl.kevinvandervlist.othi.api.model

sealed trait DataUnit

case object Watthour extends DataUnit
case object CubicMeter extends DataUnit
