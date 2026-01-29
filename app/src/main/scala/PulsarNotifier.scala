package io.msgsync.app

import zio.*
import zio.stream.*
import com.sksamuel.pulsar4s.zio.*
import com.sksamuel.pulsar4s.zio.ZioAsyncHandler.handler
import com.sksamuel.pulsar4s.Consumer
import io.msgsync.core.*
import Notifier.*
import java.util.UUID

case class PulsarNotifier(consumer: Consumer[NotifierPayload]) extends Notifier:
  override def subscribe: Stream[Throwable, Message[NotifierPayload]] =
    for (msg <- ZStream.repeatZIO(consumer.receiveAsync)) yield {
      val payload = msg.value
      val id = MessageId(UUID.nameUUIDFromBytes(msg.messageId.bytes))
      Message(id, payload)
    }

  override def start: ZIO[NotifierChannel, Throwable, Unit] =
    subscribe
      .mapZIOParUnordered(4)(msg => NotifierChannel.send(msg.payload))
      .runDrain

end PulsarNotifier

object PulsarNotifier:
  val layer
      : RLayer[Consumer[NotifierPayload] & NotifierChannel, PulsarNotifier] =
    ZLayer.scoped {
      for (consumer <- ZIO.service[Consumer[NotifierPayload]])
        yield PulsarNotifier(consumer)
    }
end PulsarNotifier
