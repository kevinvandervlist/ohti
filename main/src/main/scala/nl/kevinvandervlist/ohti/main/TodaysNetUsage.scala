package nl.kevinvandervlist.ohti.main

import java.util.UUID

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.ohti.api.PortalAPI
import nl.kevinvandervlist.ohti.api.model.{IthoZonedDateTime, MonitoringData}
import nl.kevinvandervlist.ohti.config.Config

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.Success

object TodaysNetUsage extends App with LazyLogging {
  val cfg = Config()
  logger.info(s"Starting ohti for username ${cfg.username}...")
  val portal = PortalAPI(cfg.url, cfg.username, cfg.password, debug = true)

  //val when = IthoZonedDateTime.today
  //val when = IthoZonedDateTime.fromPortalString("2019-12-31T11:00:00")
  val when = IthoZonedDateTime.fromPortalString("2019-11-20T11:00:00")
  //val provider = portal.retrieveDailyData(_, when)
  val provider = portal.retrieveYearlyData(_, when)

  def retrieve[K, T](f: K => Future[T], ids: List[K]): Future[List[T]] = Future
    .sequence(ids.map(f).map(_.transform(Success(_))))
    .map(_.collect{ case Success(data) => data })

  val gas = UUID.fromString("") // gas
  val credit = List(
    UUID.fromString(""), // low
    UUID.fromString("") // normal
  )
  val consumed = List(
    UUID.fromString(""), // low
    UUID.fromString("") // normal
  )

  val production = List(
    UUID.fromString(""), // Huis
    UUID.fromString(""), // Garage
  )

  def acc(acc: BigDecimal, v: Option[BigDecimal]): BigDecimal = v match {
    case Some(bd) => acc + bd
    case None => acc
  }
  val zero: BigDecimal = BigDecimal(0)

  val _gas = provider(gas).map(_.data.foldLeft(zero)(acc))
  val _credit = retrieve(provider, credit)
    .map(_.map(md => Some(md.data.foldLeft(zero)(acc))))
    .map(_.foldLeft(zero)(acc))
  val _consumed = retrieve(provider, consumed)
    .map(_.map(md => Some(md.data.foldLeft(zero)(acc))))
    .map(_.foldLeft(zero)(acc))
  val _production = retrieve(provider, production)
    .map(_.map(md => Some(md.data.foldLeft(zero)(acc))))
    .map(_.foldLeft(zero)(acc))

  val usage = for {
    gas <- _gas
    credit <- _credit
    consumed <- _consumed
    produced <- _production
  } yield Usage(gas, credit, consumed, produced)

  val result: Future[List[MonitoringData]] = portal.energyDevices().map(eds => {
    val today = IthoZonedDateTime.today
    eds.map(ed => portal.retrieveDailyData(ed.id, today))
  }).map(flist => Future.sequence(flist.map(_.transform(Success(_)))))
    .flatMap(_.map(_.collect{ case Success(data) => data }))


  val info = Await.result(usage, 5000 millis)
  println(s"- Gas: ${info.gas} m3")
  println(s"- Totaal verbruik: ${info.totalUsage} kWh (${info.ownProductionConsumptionPercentage}%)")
  println(s"- PV productie: ${info.produced} kWh")
  println(s"- Direct PV gebruik: ${info.directlyConsumedProduction} kWh (${info.directlyConsumedProductionPercentage}%)")
  println(s"- Netto teruglevering: ${info.credit} kWh")
  println(s"- Netto gebruik: ${info.consumed} kWh")
  println(s"- Stand salderen: ${info.compensated_net_usage} kWh")

  portal.stop()
}

case class Usage(_gas: BigDecimal, _credit: BigDecimal, _consumed: BigDecimal, _produced: BigDecimal) {
  private def m3(bd: BigDecimal): String = bd.setScale(3, BigDecimal.RoundingMode.HALF_UP).toString()
  private def kwh(bd: BigDecimal): String = (bd / 1000).setScale(2, BigDecimal.RoundingMode.HALF_UP).toString()
  private def pct(bd: BigDecimal): String = bd.setScale(2, BigDecimal.RoundingMode.HALF_UP).toString()

  def gas: String = m3(_gas)
  def credit: String = kwh(_credit)
  def consumed: String = kwh(_consumed)
  def produced: String = kwh(_produced)
  def directlyConsumedProduction: String = kwh(_directlyConsumedProduction)
  def directlyConsumedProductionPercentage: String = pct(_directlyConsumedProductionPercentage)
  def ownProductionConsumptionPercentage: String = pct(_ownProductionConsumptionPercentage)
  def totalUsage: String = kwh(_totalUsage)
  def compensated_net_usage: String = kwh(_compensated_net_usage)

  def _directlyConsumedProduction: BigDecimal = _produced - _credit
  def _directlyConsumedProductionPercentage: BigDecimal = _directlyConsumedProduction / _produced * 100
  def _ownProductionConsumptionPercentage: BigDecimal = _directlyConsumedProduction / _totalUsage * 100
  def _totalUsage: BigDecimal = _directlyConsumedProduction + _consumed
  def _compensated_net_usage: BigDecimal = _consumed - _credit
}
