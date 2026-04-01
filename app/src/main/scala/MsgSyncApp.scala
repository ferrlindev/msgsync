package io.msgsync.app

import zio.*
import zio.stream.*
import zio.config.*
import zio.config.typesafe.TypesafeConfigProvider
import com.typesafe.config.ConfigFactory
import com.sksamuel.pulsar4s.*
import PulsarConfig.given

object MsgSyncApp extends ZIOAppDefault:

  private def readResource(
      fileName: String
  )(using config: Config[PulsarConfig]): ZIO[Any, Config.Error, PulsarConfig] =
    read[PulsarConfig] {
      config.from(
        TypesafeConfigProvider
          .fromTypesafeConfig(ConfigFactory.parseResources(fileName))
          .nested("pulsar")
      )
    }

  def appLogic =
    ZIO.service[PulsarNotifier].flatMap { pulsar =>
      pulsar.subscribe.run(ZSink.drain)
    }

  override def run =
    appLogic
      .provide(
        (ZLayer.succeed(
          PulsarClient(PulsarClientConfig("pulsar://localhost:6650"))
        ) ++
          ZLayer.fromZIO(readResource("msg-pulsar.conf"))) >>>
          AppLayers.consumerLayer >>>
          PulsarNotifier.layer
      )
