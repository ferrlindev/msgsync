import sbt._
import sbt.Keys._
import sbtassembly.{MergeStrategy, PathList}
import sbtassembly.AssemblyKeys._

object Settings {
  val commonSettings = Seq(
    scalaVersion := "3.7.3",
    organization := "io.msgsync",
    version := "0.1.0-SNAPSHOT",
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:existentials",
      "-language:postfixOps",
      "-source:future",
      "-Yexplicit-nulls", // enforce null safety
      "-Wunused:all", // warn on unused imports, parameters, etc.
      // "-Werror", // treat warnings as errors to enforce code quality
      "-explain" // provide details error explanations
      // "-Wconf:msg=Compiler synthesis of Manifest:s"
    ),
    scalacOptions += "-Yretain-trees",
    Test / parallelExecution := false,
    Test / fork := true
  )

  // Assembbly settings for fat JARS
  val assemblySettings = Seq(
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", _*) => MergeStrategy.discard
      case _                        => MergeStrategy.first
    }
  )
}
