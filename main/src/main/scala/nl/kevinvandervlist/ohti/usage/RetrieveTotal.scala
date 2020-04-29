package nl.kevinvandervlist.ohti.usage

import java.util.UUID

import nl.kevinvandervlist.ohti.api.model.{IthoZonedDateTime, MonitoringData}
import nl.kevinvandervlist.ohti.repository.data.{PeriodicUsage, TimeSpan}

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

case class RetrieveScenario(name: String,
                            retriever: UUID => Future[List[MonitoringData]],
                            start: IthoZonedDateTime,
                            end: IthoZonedDateTime)

object RetrieveTotal {
  def apply(cases: Set[RetrieveScenario], devices: Devices): RetrieveTotal =
    new RetrieveTotal(cases, devices)
  def apply(`case`: RetrieveScenario, devices: Devices): RetrieveTotal =
    apply(Set(`case`), devices)
}

class RetrieveTotal(private val cases: Set[RetrieveScenario],
                    private val devices: Devices)() {

  def fetch()(implicit ec: ExecutionContext): Future[List[PeriodicUsage]] = {
    val results: immutable.Iterable[Future[PeriodicUsage]] = cases.map(retrieveTotalOfGroup)
    Future
      .sequence(results)
      .map(_.toList)
  }

  private def retrieveTotalOfGroup(r: RetrieveScenario)
                                 (implicit ec: ExecutionContext): Future[PeriodicUsage] = for {
    g <- retrieveTotalOfKind(r.retriever, devices.gas)
    fb <- retrieveTotalOfKind(r.retriever, devices.feedback)
    c <- retrieveTotalOfKind(r.retriever, devices.consumed)
    p <- retrieveTotalOfKind(r.retriever, devices.produced)
  } yield PeriodicUsage(TimeSpan(r.start.asTimeStamp, r.end.asTimeStamp), r.name, g, fb, c, p)

  private def retrieveTotalOfKind(f: UUID => Future[List[MonitoringData]], ids: List[UUID])
           (implicit ec: ExecutionContext): Future[BigDecimal] =
    retrieve(f, ids)
      .map(_.map(md => Some(md.data.foldLeft(zero)(acc))))
      .map(_.foldLeft(zero)(acc))

  private val zero: BigDecimal = BigDecimal(0)

  // Note that this is quite messy -- we just assume there is only one energy device per id
  private def retrieve[K, T](f: K => Future[List[T]], ids: List[K])
                            (implicit ec: ExecutionContext): Future[List[T]] = Future
    .sequence(ids.map(f).map(_.transform(Success(_))))
    .map(_.collect{ case Success(data) => data.head })

  private def acc(acc: BigDecimal, v: Option[BigDecimal]): BigDecimal = v match {
    case Some(bd) => acc + bd
    case None => acc
  }

}

case class Devices(gas: List[UUID], consumed: List[UUID], produced: List[UUID], feedback: List[UUID])
