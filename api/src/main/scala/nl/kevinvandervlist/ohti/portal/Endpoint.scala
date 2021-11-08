package nl.kevinvandervlist.ohti.portal

import sttp.model.Uri
import sttp.client3.UriContext

case class Endpoint(private val baseURL: String) {
  def tokens: Uri = uri"$baseURL/api/tokens"

  def energyDevices: Uri = uri"$baseURL/api/devices/energy/energyDevices"

  def monitoring(interval: Int, uuid: String, measurementCount: Int, start: Long): Uri =
    uri"$baseURL/api/monitoring/$interval/devices/$uuid/?take=$measurementCount&start=$start"

  def zones: Uri = uri"$baseURL/api/zones"

  def schedule(uuid: String): Uri = uri"$baseURL/api/schedules/$uuid"

  def devices: Uri = uri"$baseURL/api/devices"

  def device(uuid: String): Uri = uri"$baseURL/api/devices/$uuid"
}
