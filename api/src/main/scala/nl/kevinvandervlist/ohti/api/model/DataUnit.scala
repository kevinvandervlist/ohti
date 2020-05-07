package nl.kevinvandervlist.ohti.api.model

sealed trait DataUnit

case object Watthour extends DataUnit {
  override def toString: String = "Wh"
}
case object CubicMeter extends DataUnit {
  override def toString: String = "m3"
}

/** 30, celcius, .5 degree precision */
case object Celcius extends DataUnit {
  override def toString: String = "c"
}

/** 60, CO2, in PPM */
case object CO2 extends DataUnit {
  override def toString: String = "CO2"
}

/** 50, Fan speed percentage */
case object FanSpeed extends DataUnit {
  override def toString: String = "FanSpeed"
}
