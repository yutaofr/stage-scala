ThisBuild / scalaVersion := "3.3.8"
ThisBuild / organization := "ma.ath"
ThisBuild / version := "1.0.0"

lazy val circeVersion = "0.14.13"
lazy val kafkaVersion = "4.3.0"
lazy val micrometerVersion = "1.12.4"
lazy val otelVersion = "1.36.0"

lazy val root = (project in file("."))
  .enablePlugins(GatlingPlugin)
  .settings(
    name := "clearing-engine-s18-starter",
    libraryDependencies ++= Seq(
      "io.circe"            %% "circe-core"                    % circeVersion,
      "io.circe"            %% "circe-generic"                 % circeVersion,
      "io.circe"            %% "circe-parser"                  % circeVersion,
      "org.apache.kafka"     % "kafka-clients"                 % kafkaVersion,
      "com.datastax.oss"     % "java-driver-core"              % "4.17.0",
      "org.slf4j"            % "slf4j-api"                     % "2.0.12",
      "ch.qos.logback"       % "logback-classic"               % "1.5.3",
      "io.micrometer"        % "micrometer-core"               % micrometerVersion,
      "io.micrometer"        % "micrometer-registry-prometheus" % micrometerVersion,
      "io.opentelemetry"     % "opentelemetry-api"             % otelVersion,
      "io.opentelemetry"     % "opentelemetry-sdk"             % otelVersion,
      "io.gatling.highcharts" % "gatling-charts-highcharts"     % "3.11.5" % "test,it",
      "io.gatling"            % "gatling-test-framework"        % "3.11.5" % "test,it",
      "org.scalameta"       %% "munit"                         % "1.3.3" % Test
    ),
    Test / fork := true
  )
