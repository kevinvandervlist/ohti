javacOptions ++= Seq("-source", "1.11", "-target", "1.11", "-Xlint")

enablePlugins(PackPlugin)

lazy val commonSettings = Seq(
  organization := "nl.kevinvandervlist",
  name := "othi-viewer",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.13.1",
  scalacOptions ++= Seq(
    "-feature",
    "-Xfatal-warnings",
    "-deprecation",
    "-unchecked"
  ),
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.1.1" % "test"
  )
)

lazy val api = (project in file("api")).
  settings(commonSettings: _*).
  settings(
    name := "api",
    libraryDependencies ++= Seq(
    ),
  )

lazy val main = (project in file("main")).
  settings(commonSettings: _*).
  settings(
    name := "main",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
      "com.typesafe" % "config" % "1.4.0",
    ),
  ).dependsOn(api)

packMain := Map(
  // "foo" -> "classpath"
)

packJvmOpts := Map(
  // "foo" -> Seq("-Xmx512m")
)

packExtraClasspath := Map(
  // "foo" -> Seq(s"${PROG_HOME}/etc")
)

// Add a root project that explicitly depends on / aggregates _all_ modules
// See http://www.scala-sbt.org/1.0/docs/Multi-Project.html#Default+root+project for why
// lazy val minoa = Project(
//   id = "minoa",
//   base = file(".")
// ).enablePlugins(PackPlugin)
//   .dependsOn(api, main)
//   .aggregate(api, main)
