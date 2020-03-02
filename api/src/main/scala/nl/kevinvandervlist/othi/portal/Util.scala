package nl.kevinvandervlist.othi.portal

import nl.kevinvandervlist.othi.portal.TokenManager.{TokenProvider, TokenResponse}
import sttp.client.{Empty, RequestT, basicRequest}

object Util {
  def baseRequest: RequestT[Empty, Either[String, String], Nothing] = basicRequest
    .header("User-Agent", Util.UserAgent)
  val UserAgent: String = "othi-viewer API"

  def authorizedRequest(provider: TokenProvider): RequestT[Empty, Either[String, String], Nothing] = baseRequest
    .header("Authorization", s"Bearer ${provider().access_token}")
}
