package nl.kevinvandervlist.ohti.portal

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.ohti.portal.TokenManager.TokenProvider
import sttp.client3.{Empty, RequestT, basicRequest}

object Util extends LazyLogging {
  def baseRequest: RequestT[Empty, Either[String, String], Any] = basicRequest
    .header("User-Agent", Util.UserAgent)
  val UserAgent: String = "ohti API"

  def authorizedRequest(provider: TokenProvider): RequestT[Empty, Either[String, String], Any] = {
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
