import Dependencies._
import Settings._

ThisBuild / organization := "io.msgsync"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.7.3"

lazy val root = (project in file("."))
  .aggregate(core, app)
  .dependsOn(core, app)
  .settings(
    name := "msgsync",
    publishArtifact := false,
    libraryDependencies ++= Seq(
      zio,
      zioStreams,
      zioJson,
      zioTest,
      zioTestSbt,
      zioTestScalaCheck,
      zioConfig,
      zioMagnolia,
      zioTypesafe
    )
  )

lazy val core = (project in file("core"))
  .settings(
    commonSettings,
    name := "msgsync-core",
    libraryDependencies ++= Seq(
      zio,
      zioStreams,
      kyoPrelude,
      kyoCore,
      kafkaClients,
      circeCore,
      circeParser,
      circeGeneric,
      pulsar4sCore,
      pulsar4sAvro4s,
      avro4s,
      embeddedKafka,
      scalaTest,
      zioTest,
      zioTestSbt,
      zioTestScalaCheck
    )
  )

lazy val app = (project in file("app"))
  .dependsOn(core)
  .settings(
    commonSettings,
    assemblySettings,
    name := "msgsync-app",
    libraryDependencies ++= Seq(
      zio,
      circeCore,
      circeParser,
      circeGeneric,
      zioTest,
      zioTestSbt,
      zioTestScalaCheck,
      zioJson,
      zioConfig,
      zioMagnolia,
      zioTypesafe,
      pulsar4sCore,
      pulsar4sAvro4s,
      pulsar4sCirce,
      pulsar4sZio,
      pulsarJavaClient,
      scalaTest,
      scalacheck
    )
  )
