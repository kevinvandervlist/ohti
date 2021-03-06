package nl.kevinvandervlist.ohti.api.model

sealed trait Category

// 100
case object ActualTemperatureCelcius extends Category {
  override def toString: String = "ActualTemperatureCelcius"
}

// 110
case object ConfiguredTemperatureCelcius extends Category {
  override def toString: String = "ConfiguredTemperatureCelcius"
}

// 70, ppm
case object PartsPerMillion extends Category {
  override def toString: String = "PartsPerMillion"
}

// 80
case object FanSpeedPercentage extends Category {
  override def toString: String = "FanSpeedPercentage"
}

// 20
case object CentralMeterGasUsage extends Category {
  override def toString: String = "CentralMeterGasUsage"
}

// 12
case object ElectricityProduction extends Category {
  override def toString: String = "ElectricityProduction"
}

// 11
case object ElectricityUsage extends Category {
  override def toString: String = "ElectricityUsage"
}