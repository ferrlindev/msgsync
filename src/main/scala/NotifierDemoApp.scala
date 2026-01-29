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
  Producer
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

  private def makeC(client: PulsarClient, config: PulsarConfig) =
    ZLayer.succeed(
      client.consumer[NotifierPayload](
        ConsumerConfig(
          subscriptionName = Subscription(config.consumer.subscriptionName),
          topics = config.consumer.topics.map(Topic(_)),
          consumerName = Some(config.consumer.consumerName)
        )
      )
    )

  private def makeP(client: PulsarClient, topics: Set[String]) =
    ZLayer.succeed(
      MultiTopicProducer.make(
        client,
        topics.map(Topic(_))
      )
    )

  private def readResource[A](fileName: String)(using
      config: Config[A]
  ): ZIO[Any, Config.Error, A] =
    val c =
      config.from(
        TypesafeConfigProvider
          .fromTypesafeConfig(
            ConfigFactory.parseResources(fileName)
          )
          .nested("pulsar")
      )
    read[A](c)

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

  def appLogic: ZIO[
    Notifier & MultiTopicProducer & NotifierChannel,
    Throwable,
    Unit
  ] =
    for {
      notifier <- ZIO.service[Notifier]
      // stubber to publis message for push and email
      _ <- demoWithProducers
      // Start sending notifier
      _ <- notifier.start
    } yield ()

  given Config[PulsarConfig] = PulsarConfig.config

  override def run: ZIO[Any, Throwable, Unit] =
    for {
      config <- readResource("msg-pulsar.conf")
      clientConfig = PulsarClientConfig(config.serviceUrl)
      client = PulsarClient(clientConfig)
      console = ZLayer.succeed(Console.ConsoleLive)
      consumer = makeC(client, config)
      producer = makeP(client, Set("email.events", "push.events"))
      _ <- appLogic.provide(
        console,
        ViaEmail.live,
        ViaPush.live,
        NotifierChannel.live,
        PulsarNotifier.layer,
        MultiTopicProducer.layer,
        producer,
        consumer
      )
    } yield ()

end NotifierDemoApp
