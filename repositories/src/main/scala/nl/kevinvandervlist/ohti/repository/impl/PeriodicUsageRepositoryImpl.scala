package nl.kevinvandervlist.ohti.repository.impl

import java.sql.{Connection, PreparedStatement}

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.ohti.repository.PeriodicUsageRepository
import nl.kevinvandervlist.ohti.repository.data.{PeriodicUsage, TimeSpan}

import scala.util.Try

class PeriodicUsageRepositoryImpl(private val connection: Connection) extends PeriodicUsageRepository with LazyLogging {
  {
    val stmt = statement(createTableIfNotExists)
    stmt.executeUpdate()
    stmt.close()
  }

  // INTEGER can be 1-8 bytes
  // BigDecimal are stored as TEXT
  private def createTableIfNotExists: String =
    """CREATE TABLE IF NOT EXISTS periodic_usage (
      |  [from] INTEGER NOT NULL,
      |  [to] INTEGER NOT NULL,
      |  name TEXT NOT NULL,
      |  gasUsage TEXT NOT NULL,
      |  creditedPower TEXT NOT NULL,
      |  consumedPower TEXT NOT NULL,
      |  producedPower TEXT NOT NULL,
      |  directlyConsumedPowerProduction TEXT NOT NULL,
      |  directlyConsumedProductionPowerUsagePercentage TEXT NOT NULL,
      |  ownProductionPowerUsagePercentage TEXT NOT NULL,
      |  totalUsagePower TEXT NOT NULL,
      |  compensatedPowerBalance TEXT NOT NULL,
      |  PRIMARY KEY ([from], [to])
      |);
      |""".stripMargin

  private def upsertPeriodicUsage: String =
    s"""INSERT INTO periodic_usage (
      |  [from],
      |  [to],
      |  name,
      |  gasUsage,
      |  creditedPower,
      |  consumedPower,
      |  producedPower,
      |  directlyConsumedPowerProduction,
      |  directlyConsumedProductionPowerUsagePercentage,
      |  ownProductionPowerUsagePercentage,
      |  totalUsagePower,
      |  compensatedPowerBalance
      |) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      |ON CONFLICT([from], [to]) DO UPDATE SET
      |  [from]=excluded.[from],
      |  [to]=excluded.[to],
      |  name=excluded.name,
      |  gasUsage=excluded.gasUsage,
      |  creditedPower=excluded.creditedPower,
      |  consumedPower=excluded.consumedPower,
      |  producedPower=excluded.producedPower,
      |  directlyConsumedPowerProduction=excluded.directlyConsumedPowerProduction,
      |  directlyConsumedProductionPowerUsagePercentage=excluded.directlyConsumedProductionPowerUsagePercentage,
      |  ownProductionPowerUsagePercentage=excluded.ownProductionPowerUsagePercentage,
      |  totalUsagePower=excluded.totalUsagePower,
      |  compensatedPowerBalance=excluded.compensatedPowerBalance
      |;
      |""".stripMargin

  private def selectWithTimeSpan: String =
    s"""SELECT
       |  [from],
       |  [to],
       |  name,
       |  gasUsage,
       |  creditedPower,
       |  consumedPower,
       |  producedPower,
       |  directlyConsumedPowerProduction,
       |  directlyConsumedProductionPowerUsagePercentage,
       |  ownProductionPowerUsagePercentage,
       |  totalUsagePower,
       |  compensatedPowerBalance
       |FROM periodic_usage WHERE [from] = ? AND [to] = ?;
       |""".stripMargin
  private def statement(query: String): PreparedStatement = {
    val statement = connection.prepareStatement(query)
    statement.setQueryTimeout(3)
    statement
  }
  override def get(id: TimeSpan): Try[Option[PeriodicUsage]] = Try {
    val s = statement(selectWithTimeSpan)

    s.setLong(1, id.from)
    s.setLong(2, id.to)

    val rs = s.executeQuery()
    val result: Option[PeriodicUsage] = if(rs.next()) {
      Some(PeriodicUsage(
        TimeSpan(
          rs.getLong(1),
          rs.getLong(2)
        ),
        rs.getString(3),
        BigDecimal(rs.getString(4)),
        BigDecimal(rs.getString(5)),
        BigDecimal(rs.getString(6)),
        BigDecimal(rs.getString(7)),
        BigDecimal(rs.getString(8)),
        BigDecimal(rs.getString(9)),
        BigDecimal(rs.getString(10)),
        BigDecimal(rs.getString(11)),
        BigDecimal(rs.getString(12)),
      ))
    } else {
      None
    }
    s.close()
    result
  }

  override def put(item: PeriodicUsage): Try[PeriodicUsage] = Try {
    val s = statement(upsertPeriodicUsage)

    s.setLong(1, item.timeSpan.from)
    s.setLong(2, item.timeSpan.to)

    s.setString(3, String.valueOf(item.name))
    s.setString(4, String.valueOf(item.gasUsage))
    s.setString(5, String.valueOf(item.creditedPower))
    s.setString(6, String.valueOf(item.consumedPower))
    s.setString(7, String.valueOf(item.producedPower))
    s.setString(8, String.valueOf(item.directlyConsumedPowerProduction))
    s.setString(9, String.valueOf(item.directlyConsumedProductionPowerUsagePercentage))
    s.setString(10, String.valueOf(item.ownProductionPowerUsagePercentage))
    s.setString(11, String.valueOf(item.totalUsagePower))
    s.setString(12, String.valueOf(item.compensatedPowerBalance))

    s.executeUpdate()
    s.close()

    item
  }

  override def dispose(): Unit = Try {
    connection.close()
  }.getOrElse(())
}
