package nl.kevinvandervlist.ohti.repository.impl

import java.sql.Connection
import java.util.UUID

import com.typesafe.scalalogging.LazyLogging
import nl.kevinvandervlist.ohti.repository.MonitoringDataRepository
import nl.kevinvandervlist.ohti.repository.data.{MonitoringDataIndex, MonitoringDataValue}

import scala.util.Try

class MonitoringDataRepositoryImpl(protected val connection: Connection) extends SQLiteRepository with MonitoringDataRepository with LazyLogging {
  {
    val stmt = statement(createTableIfNotExists)
    stmt.executeUpdate()
    stmt.close()
  }

  private def createTableIfNotExists: String =
    """CREATE TABLE IF NOT EXISTS monitoring_data (
      |  uuid TEXT NOT NULL,
      |  [from] INTEGER NOT NULL,
      |  [to] INTEGER NOT NULL,
      |  category TEXT,
      |  unit TEXT,
      |  value TEXT,
      |  PRIMARY KEY (uuid, [from], [to], category, unit)
      |);
      |""".stripMargin

  private def upsert: String =
    s"""INSERT INTO monitoring_data (
      |  uuid,
      |  [from],
      |  [to],
      |  category,
      |  unit,
      |  value
      |) VALUES (?, ?, ?, ?, ?, ?)
      |ON CONFLICT(uuid, [from], [to], category, unit) DO UPDATE SET
      |  uuid=excluded.uuid,
      |  [from]=excluded.[from],
      |  [to]=excluded.[to],
      |  category=excluded.category,
      |  unit=excluded.unit,
      |  value=excluded.value
      |;
      |""".stripMargin

  private def select: String =
    s"""SELECT
       |  uuid,
       |  [from],
       |  [to],
       |  category,
       |  unit,
       |  value
       |FROM monitoring_data WHERE uuid = ? AND [from] = ? AND [to] = ? AND category = ? AND unit = ?;
       |""".stripMargin

  override def get(id: MonitoringDataIndex): Try[Option[MonitoringDataValue]] = Try {
    val s = statement(select)

    s.setString(1, id.deviceUUID.toString)
    s.setLong(2, id.from)
    s.setLong(3, id.to)
    s.setString(4, id.category)
    s.setString(5, id.unit)

    val rs = s.executeQuery()
    val result: Option[MonitoringDataValue] = if(rs.next()) {
      Some(MonitoringDataValue(
        MonitoringDataIndex(
          UUID.fromString(rs.getString(1)),
          rs.getLong(2),
          rs.getLong(3),
          rs.getString(4),
          rs.getString(5)
        ),
        if(rs.getString(6) == null) {
          null
        } else {
          BigDecimal(rs.getString(6))
        }
      ))
    } else {
      None
    }
    s.close()
    result
  }

  override def put(item: MonitoringDataValue): Try[MonitoringDataValue] =
    put(List(item)).map(_.head)

  override def put(items: List[MonitoringDataValue]): Try[List[MonitoringDataValue]] = Try {
    val s = statement(upsert)
    items foreach { item =>
      s.setString(1, item.index.deviceUUID.toString)
      s.setLong(2, item.index.from)
      s.setLong(3, item.index.to)
      s.setString(4, item.index.category)
      s.setString(5, item.index.unit)

      s.setString(6, if (item.value == null) null else String.valueOf(item.value))

      s.addBatch()
    }

    s.executeBatch()
    s.close()

    items
  }

  override def dispose(): Unit = Try {
    connection.close()
  }.getOrElse(())
}
