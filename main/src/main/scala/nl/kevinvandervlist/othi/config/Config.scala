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
  def password: String
}
