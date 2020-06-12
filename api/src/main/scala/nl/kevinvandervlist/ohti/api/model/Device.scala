package nl.kevinvandervlist.ohti.api.model

import java.util.UUID

case class Device(`type`: Int,
                  scheduleId: Option[UUID],
                  id: UUID,
                  name: Option[String],
                  isOnline: Boolean,
                  model: String,
                  manufacturer: String,
                  properties: List[DeviceProperty],
                  parameters: Map[String, String],
                  _etag: Long,
                  bdrSetting: Int)

case class DeviceProperty(scheduleChoices: Option[List[ScheduledChoice]],
                          label: String,
                         `type`: String,
                          typeCustom: String,
                          statusModified: Boolean,
                          id: String,
                          status: Option[String],
                          canControl: Boolean,
                          hasLogging: Boolean,
                          hasStatus: Boolean,
                          isAvailable: Boolean,
                          statusLastUpdated: Option[String])