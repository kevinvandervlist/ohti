package nl.kevinvandervlist.othi.portal

import sttp.client.{Empty, RequestT, basicRequest}

object Util {
  def baseRequest: RequestT[Empty, Either[String, String], Nothing] = basicRequest
    .header("User-Agent", Util.UserAgent)
  val UserAgent: String = "othi-viewer API"
}
