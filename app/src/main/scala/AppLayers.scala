package io.msgsync.app

import zio.*
import com.sksamuel.pulsar4s.*
import io.msgsync.core.*
import io.msgsync.core.Notifier
import io.msgsync.core.Notifier.*
import PulsarConfig.*
import com.sksamuel.avro4s.*
import com.sksamuel.pulsar4s.avro.given

object AppLayers {

  // For demo purposes only
  val producerLayer: ZLayer[
    PulsarClient & PulsarConfig,
    Throwable,
    Map[Topic, Producer[Notifier.NotifierPayload]]
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
    Notifier.NotifierPayload
  ]] =
    ZLayer.scoped {
      for {
        client <- ZIO.service[PulsarClient]
        config <- ZIO.service[PulsarConfig]
        consumer = client
          .consumer[Notifier.NotifierPayload](
            ConsumerConfig(
              subscriptionName = Subscription(config.consumer.subscriptionName),
              topics = config.consumer.topics.map(Topic(_)),
              consumerName = Some(config.consumer.consumerName)
            )
          )
      } yield consumer
    }

  val notifierLayer: ZLayer[PulsarClient & PulsarConfig, Throwable, Notifier] =
    consumerLayer >>> PulsarNotifier.layer

  val fullDemoLayer: ZLayer[
    PulsarConfig,
    Throwable,
    Notifier & NotifierChannel & MultiTopicProducer
  ] =
    ZLayer.fromZIO {
      for {
        config <- ZIO.service[PulsarConfig]
        client <- ZIO.succeed(
          PulsarClient(PulsarClientConfig(config.serviceUrl))
        )
      } yield client
    } >>> CoreLayers.channelLayer ++ notifierLayer ++ demoLayer

}
