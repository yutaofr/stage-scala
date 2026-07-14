ThisBuild / scalaVersion := "3.3.8"
ThisBuild / organization := "ma.ath.stagiaires"
ThisBuild / version := "0.4.0-SNAPSHOT"

lazy val root = project
  .in(file("."))
  .settings(
    name := "marc-clearing-engine",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test,
    Test / fork := true,
    Compile / run / fork := true,
    Compile / mainClass := Some("clearing.runMainV04")
  )
