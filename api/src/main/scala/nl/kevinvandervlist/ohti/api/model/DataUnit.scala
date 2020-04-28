package nl.kevinvandervlist.ohti.api.model

sealed trait DataUnit

case object Watthour extends DataUnit
case object CubicMeter extends DataUnit

/** 30, celcius, .5 degree precision */
case object Celcius extends DataUnit

/** 60, CO2, in PPM */
case object CO2 extends DataUnit

/** 50, Fan speed percentage */
case object FanSpeed extends DataUnit
