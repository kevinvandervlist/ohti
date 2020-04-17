package nl.kevinvandervlist.ohti.usage

import java.util.UUID

import nl.kevinvandervlist.ohti.api.model.MonitoringData

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class RetrieveTotal(private val cases: Map[String, UUID => Future[MonitoringData]],
                    private val devices: Devices)() {

  def fetch()(implicit ec: ExecutionContext): Future[List[UsageInfo]] = {
    val results: immutable.Iterable[Future[UsageInfo]] = cases.map {
      case (name, f) => retrieveTotalOfGroup(name, f)
    }
    Future
      .sequence(results)
      .map(_.toList)
  }

  private def retrieveTotalOfGroup(name: String, f: UUID => Future[MonitoringData])
                                 (implicit ec: ExecutionContext): Future[UsageInfo] = for {
    g <- retrieveTotalOfKind(f, devices.gas)
    fb <- retrieveTotalOfKind(f, devices.feedback)
    c <- retrieveTotalOfKind(f, devices.consumed)
    p <- retrieveTotalOfKind(f, devices.produced)
  } yield UsageInfo(name, g, fb, c, p)

  private def retrieveTotalOfKind(f: UUID => Future[MonitoringData], ids: List[UUID])
           (implicit ec: ExecutionContext): Future[BigDecimal] =
    retrieve(f, ids)
      .map(_.map(md => Some(md.data.foldLeft(zero)(acc))))
      .map(_.foldLeft(zero)(acc))

  private val zero: BigDecimal = BigDecimal(0)

  private def retrieve[K, T](f: K => Future[T], ids: List[K])
                            (implicit ec: ExecutionContext): Future[List[T]] = Future
    .sequence(ids.map(f).map(_.transform(Success(_))))
    .map(_.collect{ case Success(data) => data })

  private def acc(acc: BigDecimal, v: Option[BigDecimal]): BigDecimal = v match {
    case Some(bd) => acc + bd
    case None => acc
  }

}

case class Devices(gas: List[UUID], consumed: List[UUID], produced: List[UUID], feedback: List[UUID])
