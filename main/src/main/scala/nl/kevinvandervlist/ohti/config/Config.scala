package nl.kevinvandervlist.ohti.config

import com.typesafe.config.ConfigFactory

object Config {
  def apply(): Settings = {
    val conf = ConfigFactory.load().getConfig("ohti")
    new Settings {
      override def url: String = conf.getString("url")
      override def username: String = conf.getString("username")
      override def password: String = conf.getString("password")
      override def debug: Boolean = conf.getBoolean("debug")
      override def taskConfig(task: String): com.typesafe.config.Config = conf.getConfig(task)
    }
  }
}

trait Settings {
  def url: String
  def username: String
  def password: String
  def debug: Boolean
  def taskConfig(task: String): com.typesafe.config.Config
}
