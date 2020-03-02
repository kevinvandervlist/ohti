package nl.kevinvandervlist.othi.api

import java.util.UUID

import nl.kevinvandervlist.othi.api.model.{EnergyDevice, IthoZonedDateTime, MonitoringData}
import nl.kevinvandervlist.othi.portal.TokenManager.{TokenProvider, TokenResponse}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import java.util.concurrent.{ScheduledExecutorService, _}

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.othi.portal.{EnergyDevices, Monitoring, TokenManager}
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

  var scheduledRefresh: Option[ScheduledFuture[Unit]] = None

  private implicit val tokenProvider: TokenProvider = () => bearer
  private implicit val executor: ExecutionContextExecutor = ExecutionContext.fromExecutor(pool)

  // Portal features
  private val energyDevicesFeature = new EnergyDevices()
  private val monitoringFeature = new Monitoring()

  override def energyDevices: Future[List[EnergyDevice]] = Future {
    energyDevicesFeature.retrieveDevices() match {
      case Some(devices) =>
        devices
      case None =>
        logger.warn("Retrieving energy devices was successful, but no valid response found")
        List.empty
    }
  }

  override def monitoringData(interval: Int, uuid: UUID, measurementCount: Int, start: IthoZonedDateTime): Future[MonitoringData] = Future {
    monitoringFeature.retrieveMonitoringData(interval, uuid, measurementCount, start) match {
      case Some(data) =>
        data
      case None =>
        val msg = "Retrieving monitoring data was successful, but no valid response found"
        logger.warn(msg)
        throw new NoSuchElementException(msg)
    }
  }

  override def stop(): Unit = try {
    scheduledRefresh.map(_.cancel(true))
  } catch {
    case _: Exception =>
  }

  private def registerRefresh(tr: TokenResponse): Unit = {
    def refresh: Callable[Unit] = new Callable[Unit] {
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

    // Refresh the token when it is 95% away from expiring.
    val refreshAt = (tr.expires_in * 0.95).toLong
    logger.debug(s"Registering bearer token refresh call in $refreshAt seconds")
    scheduledRefresh = Some(pool.schedule(refresh, refreshAt, TimeUnit.SECONDS))
  }
}
