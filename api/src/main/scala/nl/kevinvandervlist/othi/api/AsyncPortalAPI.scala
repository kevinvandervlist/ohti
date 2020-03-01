package nl.kevinvandervlist.othi.api

import nl.kevinvandervlist.othi.api.model.EnergyDevice

import scala.concurrent.Future

private[api] class AsyncPortalAPI extends PortalAPI {
  override def energyDevices: Future[List[EnergyDevice]] = ???
}
