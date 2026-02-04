package io.msgsync.core

import zio.*
import zio.Console

object CoreLayers {
  val consoleLayer: ZLayer[Any, Nothing, Console] =
    ZLayer.succeed(Console.ConsoleLive)

  val channelLayer: ZLayer[Any, Nothing, NotifierChannel] =
    ((consoleLayer >>> (ViaEmail.live ++ ViaPush.live)) ++ consoleLayer) >>>
      NotifierChannel.live
}
