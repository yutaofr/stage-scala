ThisBuild / scalaVersion := "3.3.8"
ThisBuild / organization := "ma.ath.stagiaires"
ThisBuild / version := "2.1.0-SNAPSHOT"

val pureCoreFiles =
  "PureDomain|DataCleaner|CurriedRules|PureNettingCalculator|PureClearingEngine|PureClearingRenderer"

lazy val root = project
  .in(file("."))
  .settings(
    name := "marc-clearing-engine",
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-parallel-collections" % "1.2.0",
      "org.springframework" % "spring-context" % "6.2.19",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    ),
    Test / fork := true,
    Compile / run / fork := true,
    Compile / mainClass := Some("clearing.v21.runClearingAppV21"),
    coverageExcludedFiles :=
      s"^(?!.*[\\\\/]clearing[\\\\/]v20[\\\\/]($pureCoreFiles)$$).*$$",
    coverageFailOnMinimum := true,
    coverageMinimumStmtTotal := 100,
    coverageMinimumBranchTotal := 100
  )
