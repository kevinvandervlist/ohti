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
      |  value TEXT,
      |  unit TEXT,
      |  PRIMARY KEY (uuid, [from], [to])
      |);
      |""".stripMargin

  private def upsert: String =
    s"""INSERT INTO monitoring_data (
      |  uuid,
      |  [from],
      |  [to],
      |  value,
      |  unit
      |) VALUES (?, ?, ?, ?, ?)
      |ON CONFLICT(uuid, [from], [to]) DO UPDATE SET
      |  uuid=excluded.uuid,
      |  [from]=excluded.[from],
      |  [to]=excluded.[to],
      |  value=excluded.value,
      |  unit=excluded.unit
      |;
      |""".stripMargin

  private def select: String =
    s"""SELECT
       |  uuid,
       |  [from],
       |  [to],
       |  value,
       |  unit
       |FROM monitoring_data WHERE uuid = ? AND [from] = ? AND [to] = ?;
       |""".stripMargin

  override def get(id: MonitoringDataIndex): Try[Option[MonitoringDataValue]] = Try {
    val s = statement(select)

    s.setString(1, id.deviceUUID.toString)
    s.setLong(2, id.from)
    s.setLong(3, id.to)

    val rs = s.executeQuery()
    val result: Option[MonitoringDataValue] = if(rs.next()) {
      Some(MonitoringDataValue(
        MonitoringDataIndex(
          UUID.fromString(rs.getString(1)),
          rs.getLong(2),
          rs.getLong(3)
        ),
        if(rs.getString(4) == null) {
          null
        } else {
          BigDecimal(rs.getString(4))
        },
        rs.getString(5)
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

      s.setString(4, if (item.value == null) null else String.valueOf(item.value))
      s.setString(5, item.unit)

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
