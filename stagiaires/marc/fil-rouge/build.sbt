ThisBuild / scalaVersion := "3.3.8"
ThisBuild / organization := "ma.ath.stagiaires"
ThisBuild / version := "3.0.0-SNAPSHOT"

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
      "org.scala-lang.modules" %% "scala-parallel-collections" % "1.2.0",
      "org.springframework" % "spring-context" % "6.2.19",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "org.scalacheck" %% "scalacheck" % "1.18.1" % Test,
      "org.scalatestplus" %% "scalacheck-1-18" % "3.2.19.0" % Test
    ),
    Test / fork := true,
    Compile / run / fork := true,
    Compile / mainClass := Some("clearing.v23.runClearingAppV23"),
    coverageExcludedFiles :=
      s"^(?!.*[\\\\/]clearing[\\\\/]v20[\\\\/]($pureCoreFiles)$$).*$$",
    coverageFailOnMinimum := true,
    coverageMinimumStmtTotal := 100,
    coverageMinimumBranchTotal := 100
  )
