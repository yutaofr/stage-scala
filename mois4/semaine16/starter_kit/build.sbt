ThisBuild / scalaVersion := "3.3.8"
ThisBuild / organization := "ma.ath"
ThisBuild / version := "1.0.0"

lazy val circeVersion = "0.14.13"
lazy val kafkaVersion = "4.3.0"

lazy val root = (project in file("."))
  .settings(
    name := "clearing-engine-s16-starter",
    libraryDependencies ++= Seq(
      "io.circe"          %% "circe-core"       % circeVersion,
      "io.circe"          %% "circe-generic"    % circeVersion,
      "io.circe"          %% "circe-parser"     % circeVersion,
      "org.apache.kafka"   % "kafka-clients"    % kafkaVersion,
      "com.datastax.oss"   % "java-driver-core" % "4.17.0",
      "org.scalameta"     %% "munit"            % "1.3.3" % Test
    ),
    Test / fork := true
  )
