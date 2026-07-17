ThisBuild / scalaVersion := "3.3.8"
ThisBuild / organization := "ma.ath.stagiaires"
ThisBuild / version := "3.2.0-SNAPSHOT"

val pureCoreFiles =
  "PureDomain|DataCleaner|CurriedRules|PureNettingCalculator|PureClearingEngine|PureClearingRenderer"

lazy val root = project
  .in(file("."))
  .settings(
    name := "marc-clearing-engine",
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % "0.14.13",
      "io.circe" %% "circe-generic" % "0.14.13",
      "io.circe" %% "circe-parser" % "0.14.13",
      "org.apache.kafka" % "kafka-clients" % "4.3.0",
      "org.apache.cassandra" % "java-driver-core" % "4.19.3",
      "ch.qos.logback" % "logback-classic" % "1.5.38",
      "net.logstash.logback" % "logstash-logback-encoder" % "9.0",
      "io.micrometer" % "micrometer-registry-prometheus" % "1.16.5",
      "io.opentelemetry" % "opentelemetry-sdk" % "1.63.0",
      "io.opentelemetry" % "opentelemetry-exporter-otlp" % "1.63.0",
      "io.opentelemetry" % "opentelemetry-sdk-testing" % "1.63.0" % Test,
      "org.scala-lang.modules" %% "scala-parallel-collections" % "1.2.0",
      "org.springframework" % "spring-context" % "6.2.19",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "org.scalacheck" %% "scalacheck" % "1.18.1" % Test,
      "org.scalatestplus" %% "scalacheck-1-18" % "3.2.19.0" % Test
    ),
    Test / fork := true,
    Compile / run / fork := true,
    Compile / mainClass := Some("clearing.v32.runClearingAppV32"),
    coverageExcludedFiles :=
      s"^(?!.*[\\\\/]clearing[\\\\/]v20[\\\\/]($pureCoreFiles)$$).*$$",
    coverageFailOnMinimum := true,
    coverageMinimumStmtTotal := 100,
    coverageMinimumBranchTotal := 100
  )
