package nl.kevinvandervlist.ohti.main

import nl.kevinvandervlist.ohti.api.PortalAPI

import scala.concurrent.ExecutionContext

trait RunnableTask {
  def name: String
  def apply(api: PortalAPI)(implicit ec: ExecutionContext): Unit
}
