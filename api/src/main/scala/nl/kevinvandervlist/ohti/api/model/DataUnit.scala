package nl.kevinvandervlist.ohti.api.model

sealed trait DataUnit

case object Watthour extends DataUnit
case object CubicMeter extends DataUnit
