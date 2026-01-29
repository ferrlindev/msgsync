package io.msgsync.core

import zio.*
import zio.stream.*
import com.sksamuel.avro4s.*
import org.apache.avro.Schema
import Notifier.*

trait Notifier:
  def subscribe: Stream[Throwable, Message[NotifierPayload]]
  def start: ZIO[NotifierChannel, Throwable, Unit]

object Notifier:

  // sealed trait NotifierPayload
  case class NotifierPayload(
      payloadType: String, // e.g., "Email" or "Push"
      email: Option[Email] = None,
      push: Option[Push] = None
  )
  case class Email(recipient: EmailAddress, subject: String, body: String)
  case class Push(recipient: PushRecipient, title: String, message: String)

  given schema: Schema = AvroSchema[NotifierPayload]

  def start: ZIO[Notifier & NotifierChannel, Throwable, Unit] =
    ZIO.serviceWith[Notifier](_.start)

  def subscribe: URIO[Notifier, Stream[Throwable, Message[NotifierPayload]]] =
    ZIO.serviceWith[Notifier](_.subscribe)

end Notifier
