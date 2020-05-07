package nl.kevinvandervlist.ohti.repository.impl

import java.sql.{Connection, PreparedStatement}

trait SQLiteRepository {
  protected val connection: Connection

  protected def statement(query: String): PreparedStatement = {
    val statement = connection.prepareStatement(query)
    statement.setQueryTimeout(3)
    statement
  }

}
