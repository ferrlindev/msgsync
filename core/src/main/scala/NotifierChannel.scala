package io.msgsync.core

import zio.*
import Notifier.*

trait NotifierChannel:
  def send(payload: NotifierPayload): Task[Unit]

object NotifierChannel:
  def send(payload: NotifierPayload): ZIO[NotifierChannel, Throwable, Unit] =
    ZIO.serviceWithZIO[NotifierChannel](_.send(payload))

  val live: ZLayer[ViaEmail & ViaPush & Console, Nothing, NotifierChannel] =
    ZLayer {
      for {
        emailSender <- ZIO.service[ViaEmail]
        pushSender <- ZIO.service[ViaPush]
        console <- ZIO.service[Console]
      } yield NotifierChannelImpl(emailSender, pushSender, console)
    }

  private case class NotifierChannelImpl(
      emailSender: ViaEmail,
      pushSender: ViaPush,
      console: Console
  ) extends NotifierChannel:
    def send(payload: NotifierPayload): Task[Unit] =
      payload.payloadType.toLowerCase() match
        case "email" =>
          payload.email match
            case Some(email) => emailSender.send(email)
            case None        => console.printLine("Email payload missing.")
        case "push" =>
          payload.push match
            case Some(push) => pushSender.send(push)
            case None       => console.printLine("Push payload missing.")
        case _ =>
          console.printLine(s"Unknown payload type: ${payload.payloadType}")
