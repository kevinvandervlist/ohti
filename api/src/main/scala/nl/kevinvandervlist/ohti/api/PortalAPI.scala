package nl.kevinvandervlist.ohti.api

import java.util.UUID
import java.util.concurrent.{Executors, ScheduledExecutorService}

import sttp.client.logging.slf4j._
import nl.kevinvandervlist.ohti.api.model.{EnergyDevice, IthoZonedDateTime, MonitoringData}
import nl.kevinvandervlist.ohti.portal.Endpoint
import sttp.client.{HttpURLConnectionBackend, Identity, NothingT, SttpBackend}

import scala.concurrent.Future

object PortalAPI {
  def apply(baseURL: String, username: String, password: String, debug: Boolean = false): PortalAPI = {
    implicit val endpoint: Endpoint = Endpoint(baseURL)
    implicit val pool: ScheduledExecutorService = Executors.newScheduledThreadPool(8)
    implicit val backend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()

    if(debug) {
      new AsyncPortalAPI(username, password)(endpoint, pool, Slf4jCurlBackend[Identity, Nothing, NothingT](backend))
    } else {
      new AsyncPortalAPI(username, password)
    }
  }
}

/**
 * An opinionated, high level API wrapping the Mijn Itho Daalderop portal
 */
trait PortalAPI {
  def energyDevices(): Future[List[EnergyDevice]]
  def monitoringData(interval: Int, uuid: UUID, measurementCount: Int, start: IthoZonedDateTime): Future[MonitoringData]
  /** Stop this portal API instance (destructor) */
  def stop(): Unit = ()

  /**
   * Retrieve daily data for a given device and moment (will automatically start at the beginning of the day).
   * This is equivalent to the web portal 'day' option.
   */
  def retrieveDailyData(uuid: UUID, start: IthoZonedDateTime): Future[MonitoringData] =
    monitoringData(15, uuid, 96, start.startOfDay)

  /**
   * Retrieve weekly data for a given device and moment (will automatically start at the beginning of the day).
   * This is equivalent to the web portal 'week' option.
   */
  def retrieveWeeklyData(uuid: UUID, start: IthoZonedDateTime): Future[MonitoringData] =
    monitoringData(60, uuid, 168, start.startOfDay)

  /**
   * Retrieve monthly data for a given device and moment (will automatically start at the beginning of the day).
   * This is equivalent to the web portal 'month' option.
   */
  def retrieveMonthlyData(uuid: UUID, start: IthoZonedDateTime): Future[MonitoringData] =
    monitoringData(1440, uuid, start.daysInMonth, start.startOfDay)

  /**
   * Retrieve quarterly data for a given device and moment (will automatically start at the beginning of the day).
   */
  def retrieveQuarterlyData(uuid: UUID, start: IthoZonedDateTime): Future[MonitoringData] =
    monitoringData(1440, uuid, 90, start.startOfDay)

  /**
   * Retrieve yearly data for a given device and moment (will automatically start at the beginning of the day).
   * This is equivalent to the web portal 'year' option.
   */
  def retrieveYearlyData(uuid: UUID, start: IthoZonedDateTime): Future[MonitoringData] =
    monitoringData(10080, uuid, 52, start.startOfDay)
}