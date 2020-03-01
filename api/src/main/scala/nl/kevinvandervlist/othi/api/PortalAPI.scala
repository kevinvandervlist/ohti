package nl.kevinvandervlist.othi.api

import nl.kevinvandervlist.othi.api.model.EnergyDevice

import scala.concurrent.Future

object PortalAPI {
  def apply(): PortalAPI = new AsyncPortalAPI()
}

/**
 * An opinionated, high level API wrapping the Mijn Itho Daalderop portal
 */
trait PortalAPI {
  def energyDevices: Future[List[EnergyDevice]]
}
