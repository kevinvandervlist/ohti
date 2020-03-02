package nl.kevinvandervlist.othi.portal

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.othi.portal.TokenManager.{TokenProvider, TokenResponse}
import sttp.client.{Empty, RequestT, basicRequest}

object Util extends LazyLogging {
  def baseRequest: RequestT[Empty, Either[String, String], Nothing] = basicRequest
    .header("User-Agent", Util.UserAgent)
  val UserAgent: String = "othi-viewer API"

  def authorizedRequest(provider: TokenProvider): RequestT[Empty, Either[String, String], Nothing] = {
    provider() match {
      case Some(token) => baseRequest
        .header("Authorization", s"Bearer ${token.access_token}")
      case None =>
        val msg = "An authorized request was requested, but no token is provided"
        logger.error(msg)
        throw new IllegalStateException(msg)
    }
  }
}
