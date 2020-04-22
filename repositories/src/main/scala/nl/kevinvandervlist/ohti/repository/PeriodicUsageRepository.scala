package nl.kevinvandervlist.ohti.repository

import java.sql.DriverManager

import nl.kevinvandervlist.ohti.repository.data.{PeriodicUsage, TimeSpan}
import nl.kevinvandervlist.ohti.repository.impl.PeriodicUsageRepositoryImpl

object PeriodicUsageRepository {
  def apply(dbname: String): PeriodicUsageRepository =
    new PeriodicUsageRepositoryImpl(DriverManager.getConnection(s"jdbc:sqlite:$dbname"))
}

trait PeriodicUsageRepository extends Repository[PeriodicUsage, TimeSpan]
