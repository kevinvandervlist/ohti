package nl.kevinvandervlist.ohti.api

import java.time.temporal.ChronoUnit
import java.util.UUID
import java.util.concurrent.{Executors, ScheduledExecutorService}

import sttp.client3.logging.slf4j._
import nl.kevinvandervlist.ohti.api.model._
import nl.kevinvandervlist.ohti.portal.Endpoint
import sttp.client3.{HttpURLConnectionBackend, Identity, SttpBackend}

import scala.concurrent.Future

object PortalAPI {
  def apply(baseURL: String, username: String, password: String, debug: Boolean = false): PortalAPI = {
    implicit val endpoint: Endpoint = Endpoint(baseURL)
    implicit val pool: ScheduledExecutorService = Executors.newScheduledThreadPool(8)
    implicit val backend: SttpBackend[Identity, Any] = HttpURLConnectionBackend()

    if(debug) {
      new AsyncPortalAPI(username, password)(endpoint, pool, Slf4jLoggingBackend[Identity, Any](backend))
    } else {
      new AsyncPortalAPI(username, password)
    }
  }
}

/**
 * An opinionated, high level API wrapping the Mijn Itho Daalderop portal
 */
trait PortalAPI {
  /** List all energy devices associated with the current account */
  def energyDevices(): Future[EnergyDevices]

  /**
   * Retrieve a list of zones associated with this account.
   *
   * A zone is a room or area that has zero or more devices associated to it.
   * @return
   */
  def zones(): Future[Zones]

  /**
   * Retrieve a schedule for a given device
   *
   * @param uuid The UUID of the device that has a schedule
   * @return The schedule of a device
   */
  def retrieveSchedule(uuid: UUID): Future[Schedule]

  /**
   * Update a schedule for a given device
   *
   * @param schedule The updated schedule you want to persist
   * @return The updated schedule if succeeded
   */
  def updateSchedule(schedule: Schedule): Future[Schedule]

  /** Stop this portal API instance (destructor) */
  def stop(): Unit = ()

  /**
   * Retrieve monitoring data of a given device
   *
   * @param interval The interval (in minutes) that a single measurement should represent (minimum is 15)
   * @param uuid The UUID of the device you want to retrieve measurements of
   * @param measurementCount The number of data points to retrieve
   * @param start The timestamp denoting the initial datapoint
   * @return A series of MonitoringData elements
   */
  def monitoringData(interval: Int, uuid: UUID, measurementCount: Int, start: IthoZonedDateTime): Future[List[MonitoringData]]

  /** Retrieve all devices registered with this account
   *
   * @return All monitoring devices
   */
  def retrieveDevices(): Future[List[nl.kevinvandervlist.ohti.api.model.Device]]

  /** Retrieve details of a specific monitoring device
   *
   * @param uuid The UUID of the monitoring device
   * @return All details of the monitoring device
   */
  def retrieveDevice(uuid: UUID): Future[nl.kevinvandervlist.ohti.api.model.Device]

  /**
   * Given a device, update it in the Portal
   * @param dev The device that needs to be updated
   * @return The updated device
   */
  def updateDevice(dev: nl.kevinvandervlist.ohti.api.model.Device): Future[nl.kevinvandervlist.ohti.api.model.Device]

    /**
   * Retrieve daily data for a given device and moment (will automatically start at the beginning of the day).
   * This is equivalent to the web portal 'day' option.
   */
  def retrieveDailyData(uuid: UUID, start: IthoZonedDateTime): Future[List[MonitoringData]] =
    monitoringData(15, uuid, 96, start.startOfDay)

  /**
   * Retrieve weekly data for a given device and moment (will automatically start at the beginning of the day).
   * This is equivalent to the web portal 'week' option.
   */
  def retrieveWeeklyData(uuid: UUID, start: IthoZonedDateTime): Future[List[MonitoringData]] =
    monitoringData(60, uuid, 168, start.startOfDay)

  /**
   * Retrieve monthly data for a given device and moment (will automatically start at the beginning of the day).
   * This is equivalent to the web portal 'month' option.
   */
  def retrieveMonthlyData(uuid: UUID, start: IthoZonedDateTime): Future[List[MonitoringData]] =
    monitoringData(1440, uuid, start.daysInMonth, start.startOfDay)

  /**
   * Retrieve quarterly data for a given device and moment (will automatically start at the beginning of the day).
   */
  def retrieveQuarterlyData(uuid: UUID, start: IthoZonedDateTime): Future[List[MonitoringData]] =
    monitoringData(1440, uuid, 90, start.startOfDay)

  /**
   * Retrieve yearly data for a given device and moment (will automatically start at the beginning of the day).
   * This is equivalent to the web portal 'year' option.
   */
  def retrieveYearlyData(uuid: UUID, start: IthoZonedDateTime): Future[List[MonitoringData]] =
    monitoringData(10080, uuid, 52, start.startOfDay)

  /**
   * Retrieve yearly data for a given device and moment (will automatically start at the beginning of the day).
   * This is equivalent to the web portal 'year' option, except it can be a longer timespan
   * (but with weekly precision).
   */
  def retrieveYearlyData(uuid: UUID, _start: IthoZonedDateTime, _end: IthoZonedDateTime): Future[List[MonitoringData]] = {
    val start = _start.startOfDay
    val end = _end.endOfDay
    val measurementCount: Int = start.between(end, ChronoUnit.WEEKS).toInt
    monitoringData(10080, uuid, measurementCount, start)
  }
}