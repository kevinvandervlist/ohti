javacOptions ++= Seq("-source", "1.11", "-target", "1.11", "-Xlint")

enablePlugins(PackPlugin)

lazy val commonSettings = Seq(
  organization := "nl.kevinvandervlist",
  name := "othi",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.13.1",
  scalacOptions ++= Seq(
    "-feature",
    // "-Xfatal-warnings", disabled for now
    "-deprecation",
    "-unchecked"
  ),
  libraryDependencies ++= Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    "org.scalatest" %% "scalatest" % "3.1.1" % "test"
  )
)

lazy val api = (project in file("api")).
  settings(commonSettings: _*).
  settings(
    name := "api",
    libraryDependencies ++= Seq(
      "io.reactivex.rxjava3" % "rxjava" % "3.0.0",
      "com.softwaremill.sttp.client" %% "core" % "2.0.1",
      "com.softwaremill.sttp.client" %% "circe" % "2.0.1",
      "com.softwaremill.sttp.client" %% "slf4j-backend" % "2.0.1",
      "io.circe" %% "circe-optics" % "0.13.0"
    ),
  )

lazy val main = (project in file("main")).
  settings(commonSettings: _*).
  settings(
    name := "main",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe" % "config" % "1.4.0",
    ),
  ).dependsOn(api)
