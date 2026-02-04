package io.msgsync

import zio.*
import zio.config.*
import app.PulsarConfig
import core.*
import Notifier.*
import NotificationError.*
import io.msgsync.core.CoreLayers
import io.msgsync.app.AppLayers
import com.sksamuel.pulsar4s.Topic
import com.sksamuel.pulsar4s.avro.given
import zio.config.typesafe.TypesafeConfigProvider
import com.typesafe.config.ConfigFactory

object NotifierDemoApp extends ZIOAppDefault:

  private def readResource[A](
      fileName: String
  )(using config: Config[A]): ZIO[Any, Config.Error, A] =
    read[A] {
      config.from(
        TypesafeConfigProvider
          .fromTypesafeConfig(ConfigFactory.parseResources(fileName))
          .nested("pulsar")
      )
    }

  def demoWithProducers: ZIO[app.MultiTopicProducer, Throwable, Unit] =
    for {
      demoProducer <- ZIO.service[app.MultiTopicProducer]
      emailAddr <- ZIO
        .fromEither(EmailAddress("demo@test.com"))
        .mapError(EmailError.apply)
      pushTok <- ZIO
        .fromEither(
          PushToken(
            "1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b"
          )
        )
        .mapError(PushError.apply)
      pushRecipient = PushRecipient(pushTok, "ios")
      // Send 5 demo email messages
      _ <- ZIO.foreach(1 to 5) { i =>
        val email = Email(
          emailAddr,
          s"Demo Email Subject $i",
          s"Demo email body for message $i"
        )
        val emailPayload = NotifierPayload("Email", Some(email), None)
        demoProducer.send(Topic("email.events"), emailPayload)
      }
      // Send 5 demo push messages
      _ <- ZIO.foreach(1 to 5) { i =>
        val push = Push(
          pushRecipient,
          s"Demo Push Title $i",
          s"Demo push message for notification $i"
        )
        val pushPayload = NotifierPayload("Push", None, Some(push))
        demoProducer.send(Topic("push.events"), pushPayload)
      }
    } yield ()

  given Config[PulsarConfig] = PulsarConfig.config

  def appLogic: ZIO[
    Notifier & app.MultiTopicProducer & core.NotifierChannel,
    Throwable,
    Unit
  ] =
    for {
      notifier <- ZIO.service[Notifier]
      _ <- demoWithProducers
      _ <- notifier.start
    } yield ()

  override def run: ZIO[Any, Throwable, ExitCode] =
    appLogic
      .provide(
        AppLayers.fullDemoLayer,
        ZLayer.fromZIO(readResource("msg-pulsar.conf"))
      )
      .timeout(6.seconds)
      .foldZIO(
        error =>
          ZIO.logError(s"Application failed: $error").as(ExitCode.failure),
        _ =>
          ZIO.logInfo("Application completed successfully").as(ExitCode.success)
      )
