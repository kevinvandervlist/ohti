package nl.kevinvandervlist.ohti.main

import nl.kevinvandervlist.ohti.api.PortalAPI
import nl.kevinvandervlist.ohti.config.Settings

import scala.concurrent.ExecutionContext

trait RunnableTask {
  def name: String
  def apply(api: PortalAPI, settings: Settings)(implicit ec: ExecutionContext): Unit
}
