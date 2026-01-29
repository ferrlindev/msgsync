package io.msgsync.core

import zio.*
import Notifier.Email

trait ViaEmail:
  def send(email: Email): Task[Unit]

object ViaEmail:
  def send(email: Email): ZIO[ViaEmail, Throwable, Unit] =
    ZIO.serviceWithZIO[ViaEmail](_.send(email))

  val live: ZLayer[Console, Nothing, ViaEmail] =
    ZLayer {
      for {
        console <- ZIO.service[Console]
      } yield new ViaEmail {
        def send(email: Email): Task[Unit] =
          console.printLine(
            s"Sending via email channel to ${email.recipient}: ${email.subject} and body ${email.body}"
          )
      }
    }
