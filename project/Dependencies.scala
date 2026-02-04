import sbt._

object Dependencies {
  object Versions {
    val kyo = "1.0-RC1"
    val kafka = "4.1.0"
    val embeddedKafka = "4.1.0"
    val scalaTest = "3.2.19"
    val zio = "2.1.22"
    val zioJson = "0.8.0"
    val scalacheck = "1.18.1"

    val circe = "0.14.15"
    val zioConfig = "4.0.6"
    val zioLogging = "2.5.3"

    val pulsar4s = "2.12.0.1"
    val avro4s = "5.0.14"

    // val slf4j = "2.7.1"
    val pulsarJava = "4.1.2"
  }

  val kyoPrelude = "io.getkyo" %% "kyo-prelude" % Versions.kyo
  val kyoCore = "io.getkyo" %% "kyo-core" % Versions.kyo
  val kafkaClients = "org.apache.kafka" % "kafka-clients" % Versions.kafka
  val embeddedKafka =
    "io.github.embeddedkafka" %% "embedded-kafka" % Versions.embeddedKafka % Test
  val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest % Test
  val zio = "dev.zio" %% "zio" % Versions.zio
  val zioStreams = "dev.zio" %% "zio-streams" % Versions.zio
  val zioJson = "dev.zio" %% "zio-json" % Versions.zioJson
  val zioLogging = "dev.zio" %% "zio-logging" % Versions.zioLogging

  val zioTest = "dev.zio" %% "zio-test" % Versions.zio % Test
  val zioTestSbt = "dev.zio" %% "zio-test-sbt" % Versions.zio % Test
  val zioTestScalaCheck =
    "dev.zio" %% "zio-test-scalacheck" % Versions.zio % Test
  val scalacheck = "org.scalacheck" %% "scalacheck" % Versions.scalacheck % Test

  val circeCore = "io.circe" %% "circe-core" % Versions.circe
  val circeParser = "io.circe" %% "circe-parser" % Versions.circe
  val circeGeneric = "io.circe" %% "circe-generic" % Versions.circe

  val zioConfig = "dev.zio" %% "zio-config" % Versions.zioConfig
  val zioMagnolia = "dev.zio" %% "zio-config-magnolia" % Versions.zioConfig
  val zioTypesafe = "dev.zio" %% "zio-config-typesafe" % Versions.zioConfig

  val pulsar4sCirce =
    "com.clever-cloud.pulsar4s" %% "pulsar4s-circe" % Versions.pulsar4s
  val pulsar4sAvro4s =
    "com.clever-cloud.pulsar4s" %% "pulsar4s-avro" % Versions.pulsar4s
  val pulsar4sCore =
    "com.clever-cloud.pulsar4s" %% "pulsar4s-core" % Versions.pulsar4s
  val pulsar4sZio =
    "com.clever-cloud.pulsar4s" %% "pulsar4s-zio" % Versions.pulsar4s

  val pulsarJavaClient =
    "org.apache.pulsar" % "pulsar-client" % Versions.pulsarJava

  val avro4s =
    "com.sksamuel.avro4s" %% "avro4s-core" % Versions.avro4s
}
