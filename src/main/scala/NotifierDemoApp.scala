package io.msgsync

import app.*
import zio.*
import zio.config.*
import core.*
import Notifier.*
import NotificationError.*
import core.given
import NotifierChannel.given
import com.sksamuel.pulsar4s.{
  PulsarClient,
  Subscription,
  Topic,
  PulsarClientConfig,
  ConsumerConfig,
  ProducerConfig,
  Producer,
  Consumer
}
import com.sksamuel.pulsar4s.zio.*
import com.sksamuel.pulsar4s.avro.*
import com.sksamuel.avro4s.*
import org.apache.avro.Schema
import zio.config.typesafe.TypesafeConfigProvider
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration.*
import java.io.File
import java.nio.file.Paths
import zio.config.ConfigOps
import com.sksamuel.pulsar4s.DefaultProducerMessage

object NotifierDemoApp extends ZIOAppDefault:

  private def readResource[A](fileName: String)(using
      config: Config[A]
  ): ZIO[Any, Config.Error, A] =
    read[A] {
      config.from(
        TypesafeConfigProvider
          .fromTypesafeConfig(
            ConfigFactory.parseResources(fileName)
          )
          .nested("pulsar")
      )
    }

  def demoWithProducers: ZIO[MultiTopicProducer, Throwable, Unit] =
    for {
      demoProducer <- ZIO.service[MultiTopicProducer]
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
        val emailPayload = NotifierPayload(
          payloadType = "Email",
          email = Some(email),
          push = None
        )
        demoProducer
          .send(Topic("email.events"), emailPayload)
      }
      // Send 5 demo push messages
      _ <- ZIO.foreach(1 to 5) { i =>
        val push = Push(
          pushRecipient,
          s"Demo Push Title $i",
          s"Demo push message for notification $i"
        )
        val pushPayload = NotifierPayload(
          payloadType = "Push",
          email = None,
          push = Some(push)
        )
        demoProducer
          .send(Topic("push.events"), pushPayload)
      }
    } yield ()

  given Config[PulsarConfig] = PulsarConfig.config

  // For demo purposes only
  val producerLayer: ZLayer[
    PulsarClient & PulsarConfig,
    Throwable,
    Map[Topic, Producer[NotifierPayload]]
  ] =
    ZLayer.scoped {
      for {
        client <- ZIO.service[PulsarClient]
        config <- ZIO.service[PulsarConfig]
      } yield MultiTopicProducer.make(
        client,
        config.consumer.topics.map(Topic(_)).toSet
      )
    }

  val demoLayer = producerLayer >>> MultiTopicProducer.layer

  val consumerLayer: ZLayer[PulsarClient & PulsarConfig, Throwable, Consumer[
    NotifierPayload
  ]] =
    ZLayer.scoped {
      for {
        client <- ZIO.service[PulsarClient]
        config <- ZIO.service[PulsarConfig]
        consumer = client
          .consumer[NotifierPayload](
            ConsumerConfig(
              subscriptionName = Subscription(config.consumer.subscriptionName),
              topics = config.consumer.topics.map(Topic(_)),
              consumerName = Some(config.consumer.consumerName)
            )
          )
      } yield consumer
    }

  val notifierLayer: ZLayer[
    PulsarClient & PulsarConfig,
    Throwable,
    Notifier
  ] = consumerLayer >>> PulsarNotifier.layer

  val channelLayer = ((ZLayer.succeed(Console.ConsoleLive) >>>
    (ViaEmail.live ++ ViaPush.live)) ++
    ZLayer.succeed(Console.ConsoleLive)) >>>
    NotifierChannel.live

  val fullLayer = ZLayer {
    for {
      config <- ZIO.service[PulsarConfig]
      client = PulsarClient(PulsarClientConfig(config.serviceUrl))
    } yield client
  } >>> channelLayer ++ notifierLayer ++ demoLayer

  def appLogic
      : ZIO[Notifier & MultiTopicProducer & NotifierChannel, Throwable, Unit] =
    for {
      config <- readResource("msg-pulsar.conf")
      notifier <- ZIO.service[Notifier]
      // stubber to publis message for push and email
      _ <- demoWithProducers
      // Start sending notifier
      _ <- notifier.start
    } yield ()

  override def run: ZIO[Any, Throwable, Unit] =
    for {
      config <- readResource("msg-pulsar.conf")
      _ <- appLogic
        .provideLayer(fullLayer)
        .provide(ZLayer.succeed(config))
    } yield ()

end NotifierDemoApp
