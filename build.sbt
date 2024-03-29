javacOptions ++= Seq("-source", "1.11", "-target", "1.11", "-Xlint")

lazy val commonSettings = Seq(
  organization := "nl.kevinvandervlist",
  name := "ohti",
  version := "0.3.0",
  scalaVersion := "2.13.4",
  scalacOptions ++= Seq(
    "-feature",
    // "-Werror", disabled for now
    "-deprecation",
    "-unchecked",
    "-Wdead-code",
    "-Wunused:imports,patvars,privates,locals,imports,explicits,implicits,params,linted",
    "-Xlint:deprecation"
  ),
  libraryDependencies ++= Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
    "org.scalatest" %% "scalatest" % "3.2.10" % "test"
  ),
  // Skip tests in assembly
  test in assembly := {},
)

lazy val api = (project in file("api")).
  settings(commonSettings: _*).
  settings(
    name := "api",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client3" %% "core" % "3.3.16",
      "com.softwaremill.sttp.client3" %% "circe" % "3.3.16",
      "com.softwaremill.sttp.client3" %% "slf4j-backend" % "3.3.16",
      "io.circe" %% "circe-optics" % "0.14.1"
    ),
  ).settings(
    assemblyJarName in assembly := "api.jar",
  )

lazy val repositories = (project in file("repositories")).
  settings(commonSettings: _*).
  settings(
    name := "repositories",
    libraryDependencies ++= Seq(
      "org.xerial" % "sqlite-jdbc" % "3.36.0.3"
    ),
  ).settings(
    assemblyJarName in assembly := "repositories.jar",
  ).dependsOn(api)

lazy val main = (project in file("main")).
  settings(commonSettings: _*).
  settings(
    name := "main",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.6",
      "com.typesafe" % "config" % "1.4.1",
    ),
  ).settings(
    mainClass in assembly := Some("nl.kevinvandervlist.ohti.main.Main"),
).dependsOn(api, repositories)
