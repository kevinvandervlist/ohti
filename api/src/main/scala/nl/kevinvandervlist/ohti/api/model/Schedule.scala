package nl.kevinvandervlist.ohti.api.model

import java.time.DayOfWeek
import java.util.UUID

case class Schedule(id: UUID, components: List[ScheduledDevice], properties: List[ScheduledProperty])

case class ScheduledDevice(id: UUID, isActive: Boolean)

case class ScheduledProperty(id: String, choices: List[ScheduledChoice])

case class ScheduledChoice(label: String, value: String, isDisabled: Boolean, moments: List[Moment])

case class Moment(day: DayOfWeek, time: Long)
