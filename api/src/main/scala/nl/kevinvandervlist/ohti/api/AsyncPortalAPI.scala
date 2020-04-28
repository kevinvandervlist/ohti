package nl.kevinvandervlist.ohti.api

import java.util.UUID

import nl.kevinvandervlist.ohti.api.model.{EnergyDevice, IthoZonedDateTime, MonitoringData, Zone}
import nl.kevinvandervlist.ohti.portal.TokenManager.{TokenProvider, TokenResponse}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import java.util.concurrent.{ScheduledExecutorService, _}

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.ohti.portal.{Endpoint, EnergyDevices, Monitoring, TokenManager, Zones}
import sttp.client.{Identity, NothingT, SttpBackend}

private[api] class AsyncPortalAPI(username: String, password: String)
                                 (implicit val endpoint: Endpoint,
                                  implicit val pool: ScheduledExecutorService,
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
  private val zonesFeature = new Zones()

  override def stop(): Unit = try {
    scheduledRefresh.map(_.cancel(true))
    // A hack required to make sure delayed tasks are not executed after shutdown.
    pool match {
      // https://bugs.java.com/bugdatabase/view_bug.do?bug_id=7069418
      case e: ScheduledThreadPoolExecutor => e.setExecuteExistingDelayedTasksAfterShutdownPolicy(false)
      case _ => //
    }
    pool.shutdown()
    logger.info("Stopping pool...")
  } catch {
    case e: Exception =>
      logger.error("Failed to stop: {}", e)
  }

  private def registerRefresh(tr: TokenResponse): Unit = {
    def refresh: Callable[Unit] = new Callable[Unit] {
      override def call(): Unit = {
        bearer.flatMap(tokenManager.refreshToken) match {
          case bt@Some(updated) =>
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

  private def logFailure[T](result: Option[T], msg: String): Option[T] = result match {
    case result: Some[T] => result
    case None =>
      logger.warn(msg)
      None
  }

  override def energyDevices(): Future[List[EnergyDevice]] = Future {
    logFailure(energyDevicesFeature.retrieveDevices(),
      "Retrieving energy devices was successful, but no valid response found"
    ) getOrElse List.empty
  }

  override def monitoringData(interval: Int, uuid: UUID, measurementCount: Int, start: IthoZonedDateTime): Future[List[MonitoringData]] = Future {
    val msg = "Retrieving monitoring data was successful, but no valid response found"
    logFailure(monitoringFeature.retrieveMonitoringData(interval, uuid, measurementCount, start),
      msg
    ) match {
      case Some(result) => result
      case None => throw new NoSuchElementException(msg)
    }
  }

  override def zones(): Future[List[Zone]] = Future {
    logFailure(zonesFeature.retrieveZones(),
      "Retrieving zones succeeded, but no valid response found."
    ) getOrElse List.empty
  }
}