package nl.kevinvandervlist.ohti.portal

import sttp.model.Uri
import sttp.client.UriContext

case class Endpoint(private val baseURL: String) {
  def tokens: Uri = uri"$baseURL/api/tokens"

  def energyDevices: Uri = uri"$baseURL/api/devices/energy/energyDevices"

  def monitoring(interval: Int, uuid: String, measurementCount: Int, start: Long): Uri = {
    uri"$baseURL/api/monitoring/$interval/devices/$uuid/?take=$measurementCount&start=$start"
  }
}
