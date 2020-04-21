package nl.kevinvandervlist.ohti.usage

import java.util.UUID

import com.typesafe.config.Config
import scala.jdk.CollectionConverters._

trait KnownDevices {
  private def uuidList(sl: java.util.List[String]): List[UUID] = sl.asScala
    .toList
    .map(UUID.fromString)

  def devices(cfg: Config): Devices = Devices(
    gas = uuidList(cfg.getStringList("gas")),
    consumed = uuidList(cfg.getStringList("consumed")),
    produced = uuidList(cfg.getStringList("produced")),
    feedback = uuidList(cfg.getStringList("feedback"))
  )
}
