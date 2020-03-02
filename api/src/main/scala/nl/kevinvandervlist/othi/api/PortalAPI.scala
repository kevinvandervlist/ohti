package nl.kevinvandervlist.othi.api

import java.util.concurrent.{Executors, ScheduledExecutorService}

import nl.kevinvandervlist.othi.api.model.EnergyDevice
import sttp.client.HttpURLConnectionBackend

import scala.concurrent.Future

object PortalAPI {
  private implicit val pool: ScheduledExecutorService = Executors.newScheduledThreadPool(4)
  private implicit val backend = HttpURLConnectionBackend()
  def apply(username: String, password: String): PortalAPI = new AsyncPortalAPI(username, password)
}

/**
 * An opinionated, high level API wrapping the Mijn Itho Daalderop portal
 */
trait PortalAPI {
  def energyDevices: Future[List[EnergyDevice]]
}
