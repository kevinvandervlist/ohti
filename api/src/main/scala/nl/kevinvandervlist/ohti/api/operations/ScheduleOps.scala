package nl.kevinvandervlist.ohti.api.operations

import nl.kevinvandervlist.ohti.api.model.{IthoZonedDateTime, Schedule, ScheduledChoice}

import scala.language.implicitConversions

object ScheduleOps {
  implicit def richSchedule(schedule: Schedule): ScheduleOps = new ScheduleOps(schedule)
  implicit def plainSchedule(schedule: ScheduleOps): Schedule = schedule.schedule
}

class ScheduleOps(val schedule: Schedule) {
  def assignSetpoint(choices: List[ScheduledChoice]): Option[ScheduleOps] = {
    val updatedProperties = schedule.properties.map {
      case p if p.id == "SetpointTemperature" => p.copy(choices = choices)
      case otherwise => otherwise
    }
    if(updatedProperties == schedule.properties) {
      None
    } else {
      Some(schedule.copy(
        properties = updatedProperties,
        updatedAt = IthoZonedDateTime.today
      ))
    }
  }
}