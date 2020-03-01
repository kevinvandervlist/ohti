package nl.kevinvandervlist.othi.main

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.othi.config.Config

object Main extends App with LazyLogging {
  val cfg = Config()
  logger.info(s"Starting othi-viewer for username ${cfg.username}...")
}
