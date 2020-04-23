package nl.kevinvandervlist.ohti.main

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.ohti.api.PortalAPI
import nl.kevinvandervlist.ohti.config.Config

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object Main extends App with LazyLogging {
  val tasks: Map[String, RunnableTask] = Map(
    ListDevices.name -> ListDevices,
    UsageDetails.name -> UsageDetails,
    DailyAggregateSQLite.name -> DailyAggregateSQLite,
    CatchupDailyAggregateSQLite.name -> CatchupDailyAggregateSQLite
  )

  if(args.length == 0) {
    help()
    System.exit(1)
  }

  val queue: mutable.Stack[RunnableTask] = mutable.Stack()
  for(arg <- args) {
    arg match {
      case "help" => help()
      case task if tasks.contains(task) => queue.addOne(tasks(task))
      case unknown => logger.error("Unknown task {}", unknown)
    }
  }

  if(queue.nonEmpty) {
    val cfg = Config()
    logger.info(s"Starting ohti for username ${cfg.username}...")
    val portal = PortalAPI(cfg.url, cfg.username, cfg.password, debug = cfg.debug)
    for(task <- queue) {
      logger.info("Executing task {}", task.name)
      task.apply(portal, cfg)
      logger.info("Completed task {}", task.name)
    }

    portal.stop()
  }

  private def help(): Unit = {
    logger.error("Available tasks are:")
    for(n <- tasks.keys) {
      logger.error(s"* $n")
    }
  }
}
