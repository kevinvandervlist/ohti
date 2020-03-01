package nl.kevinvandervlist.othi.config

import com.typesafe.config.ConfigFactory

object Config {
  def apply(): Settings = {
    val conf = ConfigFactory.load().getConfig("othi-viewer")
    new Settings {
      override def username: String = conf.getString("username")
      override def password: String = conf.getString("password")
    }
  }
}

trait Settings {
  def username: String
  /** Itho uses a hexademical encoding for the password when sending it to the login endpoint */
  def encodedUsername: String = username
    .toList
    .map(_.toInt.toHexString)
    .mkString
  def password: String
}
