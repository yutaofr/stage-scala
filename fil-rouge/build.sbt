ThisBuild / scalaVersion := "3.3.8"
ThisBuild / organization := "ma.ath"
ThisBuild / version := "2.4.0-SNAPSHOT"

lazy val circeVersion = "0.14.13"
lazy val kafkaVersion = "4.3.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "clearing-engine-fil-rouge",
    libraryDependencies ++= Seq(
      "io.circe"          %% "circe-core"       % circeVersion,
      "io.circe"          %% "circe-generic"    % circeVersion,
      "io.circe"          %% "circe-parser"     % circeVersion,
      "org.apache.kafka"   % "kafka-clients"    % kafkaVersion,
      "com.datastax.oss"   % "java-driver-core" % "4.17.0",
      "org.scalameta"     %% "munit"            % "1.3.3" % Test
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
