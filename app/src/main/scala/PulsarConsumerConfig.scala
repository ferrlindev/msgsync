package io.msgsync.app

import zio.*
import zio.config.*
import zio.config.magnolia.*

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
  val config: Config[PulsarConfig] =
    deriveConfig[PulsarConfig]
