javacOptions ++= Seq("-source", "1.11", "-target", "1.11", "-Xlint")

lazy val commonSettings = Seq(
  organization := "nl.kevinvandervlist",
  name := "ohti",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.13.2",
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
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    "org.scalatest" %% "scalatest" % "3.1.1" % "test"
  ),
  // Skip tests in assembly
  test in assembly := {},
)

lazy val api = (project in file("api")).
  settings(commonSettings: _*).
  settings(
    name := "api",
    libraryDependencies ++= Seq(
      "io.reactivex.rxjava3" % "rxjava" % "3.0.2",
      "com.softwaremill.sttp.client" %% "core" % "2.0.9",
      "com.softwaremill.sttp.client" %% "circe" % "2.0.9",
      "com.softwaremill.sttp.client" %% "slf4j-backend" % "2.0.9",
      "io.circe" %% "circe-optics" % "0.13.0"
    ),
  ).settings(
    assemblyJarName in assembly := "api.jar",
  )

lazy val repositories = (project in file("repositories")).
  settings(commonSettings: _*).
  settings(
    name := "repositories",
    libraryDependencies ++= Seq(
      "org.xerial" % "sqlite-jdbc" % "3.30.1"
    ),
  ).settings(
    assemblyJarName in assembly := "repositories.jar",
  )

lazy val main = (project in file("main")).
  settings(commonSettings: _*).
  settings(
    name := "main",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe" % "config" % "1.4.0",
    ),
  ).settings(
    mainClass in assembly := Some("nl.kevinvandervlist.ohti.main.Main"),
) .dependsOn(api, repositories)
