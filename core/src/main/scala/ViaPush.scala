package io.msgsync.core

import zio.*
import Notifier.Push

trait ViaPush:
  def send(push: Push): Task[Unit]

object ViaPush:
  def send(push: Push): ZIO[ViaPush, Throwable, Unit] =
    ZIO.serviceWithZIO[ViaPush](_.send(push))

  val live: ZLayer[Console, Nothing, ViaPush] =
    ZLayer {
      for {
        console <- ZIO.service[Console]
      } yield new ViaPush {
        def send(push: Push): Task[Unit] =
          console.printLine(
            s"Sending via push notification channel to ${push.recipient}: ${push.message}"
          )
      }
    }
