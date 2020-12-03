package nl.kevinvandervlist.ohti.api.model

import java.time.DayOfWeek
import java.util.UUID

case class Schedule(deviceType: Int,
                    id: UUID,
                    components: List[ScheduledDevice],
                    properties: List[ScheduledProperty],
                    user: String,
                    createdAt: IthoZonedDateTime,
                    updatedAt: IthoZonedDateTime
                   )

case class ScheduledDevice(id: UUID, isActive: Boolean)

case class ScheduledProperty(id: String, choices: List[ScheduledChoice])

case class ScheduledChoice(label: String, value: String, isDisabled: Boolean, moments: Option[List[Moment]], overrideUntil: Long)

case class Moment(day: DayOfWeek, time: Long)
