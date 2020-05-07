package nl.kevinvandervlist.ohti.repository

import java.sql.DriverManager

import nl.kevinvandervlist.ohti.repository.data.{MonitoringDataIndex, MonitoringDataValue}
import nl.kevinvandervlist.ohti.repository.impl.MonitoringDataRepositoryImpl

import scala.util.Try

object MonitoringDataRepository {
  def apply(dbname: String): MonitoringDataRepository =
    new MonitoringDataRepositoryImpl(DriverManager.getConnection(s"jdbc:sqlite:$dbname"))
}

trait MonitoringDataRepository extends Repository[MonitoringDataValue, MonitoringDataIndex] {
  def put(items: List[MonitoringDataValue]): Try[List[MonitoringDataValue]]
}
