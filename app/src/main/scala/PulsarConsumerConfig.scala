package io.msgsync.app

import zio.*
import com.typesafe.config.ConfigFactory
import java.time.Duration

object ConsumerConfig:
  def hoconDuration(key: String): Config[Duration] =
    Config.string(key).mapAttempt { s =>
      ConfigFactory.parseString(s"v = $s").getDuration("v")
    }

  given Config[ConsumerConfig] =
    (
      Config.listOf("topics", Config.string) ++
        Config.string("subscriptionName") ++
        Config.string("subscriptionType") ++
        Config.string("consumerName") ++
        Config.string("subscriptionInitialPosition") ++
        Config.int("receiverQueueSize") ++
        Config.int("maxTotalReceiverQueueSize") ++
        hoconDuration("ackTimeout") ++
        hoconDuration("negativeAckRedeliveryDelay") ++
        Config.boolean("enableRetryOnDeadLetter")
    ).map(ConsumerConfig.apply)

case class ConsumerConfig(
    topics: List[String],
    subscriptionName: String,
    subscriptionType: String,
    consumerName: String,
    subscriptionInitialPosition: String,
    receiverQueueSize: Int,
    maxTotalReceiverQueueSize: Int,
    ackTimeout: Duration,
    negativeAckRedeliveryDelay: Duration,
    enableRetryOnDeadLetter: Boolean
)

case class PulsarConfig(
    serviceUrl: String,
    consumer: ConsumerConfig
)

object PulsarConfig:
  given Config[PulsarConfig] =
    (
      Config.string("serviceUrl") ++
        summon[Config[ConsumerConfig]].nested("consumer")
    ).map(PulsarConfig.apply)
