package nl.kevinvandervlist.ohti.usage

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.ohti.repository.PeriodicUsageRepository
import nl.kevinvandervlist.ohti.repository.data.{PeriodicUsage, TimeSpan}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class PeriodicAggregateSQLite(repo: PeriodicUsageRepository, devices: Devices)(implicit ec: ExecutionContext) extends LazyLogging {

  def aggregate(scenario: RetrieveScenario): Future[PeriodicUsage] = {
    val ts = TimeSpan(scenario.start.asTimeStamp, scenario.end.asTimeStamp)

    repo.get(ts) match {
      case Failure(ex) =>
        logger.error("The repository failed: {}", ex)
        Future { ??? }
      case Success(Some(pu)) =>
        logger.info(s"Aggregate for {} already exists", ts)
        Future { pu }
      case Success(None) =>
        logger.info("Aggregated statistics for timespan {} do not exist yet, retrieving them.", ts)
        RetrieveTotal(scenario, devices).fetch()
          .map(_.head)
          .andThen {
            case Success(pu: PeriodicUsage) => repo.put(pu) match {
              case Success(i) => logger.info(s"Persisted daily aggregate {}", i.name)
              case Failure(ex) => logger.error("Unable to persist daily aggregate: {}", ex)
            }
          }
    }
  }
}

