ThisBuild / scalaVersion := "3.3.8"
ThisBuild / organization := "ma.ath"
ThisBuild / version := "2.4.0-SNAPSHOT"

lazy val zioVersion = "2.1.26"
lazy val zioJsonVersion = "0.9.2"
lazy val kafkaVersion = "4.3.0"
lazy val tapirVersion = "1.13.23"

lazy val root = project
  .in(file("."))
  .settings(
    name := "clearing-engine-fil-rouge",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-json" % zioJsonVersion,
      "org.apache.kafka" % "kafka-clients" % kafkaVersion,
      "com.datastax.oss" % "java-driver-core" % "4.17.0",
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-zio" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "org.scalameta" %% "munit" % "1.3.3" % Test
    ),
    Test / fork := true,
    Compile / mainClass := Some("distributed.http.ClearingServer"),
    assembly / mainClass := Some("distributed.http.ClearingServer"),
    assembly / assemblyJarName := "clearing-engine.jar",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "resources", _*) => MergeStrategy.first
      case PathList("META-INF", "maven", "org.webjars", "swagger-ui", _*) =>
        MergeStrategy.first
      case PathList("META-INF", "services", _*) => MergeStrategy.concat
      case PathList("META-INF", _*)             => MergeStrategy.discard
      case _                                    => MergeStrategy.first
    }
  )
