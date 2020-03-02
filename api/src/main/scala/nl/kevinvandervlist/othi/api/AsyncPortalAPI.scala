package nl.kevinvandervlist.othi.api

import nl.kevinvandervlist.othi.api.model.EnergyDevice
import nl.kevinvandervlist.othi.portal.TokenManager.TokenResponse

import scala.concurrent.Future
import java.util.concurrent.{ScheduledExecutorService, _}

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.othi.portal.TokenManager
import sttp.client.{Identity, NothingT, SttpBackend}

private[api] class AsyncPortalAPI(username: String, password: String)
                                 (implicit val pool: ScheduledExecutorService,
                                  implicit val backend: SttpBackend[Identity, Nothing, NothingT]
                                 ) extends PortalAPI with LazyLogging {
  private val tokenManager = new TokenManager()

  // Request the initial bearer token. Do this blocking, because if it fails we are of no use.
  private var bearer: Option[TokenResponse] = tokenManager.requestToken(username, password)
  // And immediately register a refresh of the token
  bearer match {
    case Some(tr) => registerRefresh(tr)
    case None =>
      val msg = s"Not able to request a bearer token for $username at the remote service"
      logger.error(msg)
      throw new IllegalStateException(msg)
  }

  override def energyDevices: Future[List[EnergyDevice]] = ???

  private def refresh: Callable[Unit] = new Callable[Unit] {
    override def call(): Unit = {
      bearer.flatMap(tokenManager.refreshToken) match {
        case bt @ Some(updated) =>
          bearer = bt
          registerRefresh(updated)
          logger.debug(s"Refreshed bearer token for $username successfully")
        case None =>
          logger.error("Refreshing bearer token failed; no new token received")
      }
    }
  }

  private def registerRefresh(tr: TokenResponse): Unit = {
    // Refresh the token when it is 95% away from expiring.
    val refreshAt = (tr.expires_in * 0.95).toLong
    logger.debug(s"Registering bearer token refresh call in $refreshAt seconds")
    pool.schedule(refresh, refreshAt, TimeUnit.SECONDS)
  }
}
