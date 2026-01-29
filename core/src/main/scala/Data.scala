package io.msgsync.core

import java.util.UUID

type MessageId = MessageId.T
object MessageId:
  opaque type T = UUID
  def apply(uuid: UUID): MessageId = uuid
  def random: MessageId = UUID.randomUUID()
  extension (id: MessageId) def toUUID: UUID = id

final case class Message[A](id: MessageId, payload: A)
