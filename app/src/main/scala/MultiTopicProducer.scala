package io.msgsync.app

import zio.*
import io.msgsync.core.Notifier
import io.msgsync.core.Notifier.*
import org.apache.pulsar.client.api.Schema
import com.sksamuel.pulsar4s.{Producer, PulsarClient, ProducerConfig, Topic}

import scala.util.{Success, Failure}

/** for Demo purposes only */
case class MultiTopicProducer(
    producers: Ref[Map[Topic, Producer[Notifier.NotifierPayload]]]
):
  def send(topic: Topic, message: Notifier.NotifierPayload): Task[Unit] =
    for {
      dict <- producers.get
      _ <- dict.get(topic) match {
        case Some(p) =>
          ZIO.attempt {
            p.send(message) match {
              case Success(id) => id
              case Failure(ex) => throw ex
            }
          }
        case None => ZIO.fail(new Exception(s"No producer for topic $topic"))
      }
    } yield ()

object MultiTopicProducer:
  def make(
      client: PulsarClient,
      topics: Set[Topic]
  )(using Schema[Notifier.NotifierPayload]): Map[Topic, Producer[Notifier.NotifierPayload]] =
    topics.zipWithIndex.map { (topic, index) =>
      topic -> client.producer[Notifier.NotifierPayload](
        ProducerConfig(
          topic = topic,
          producerName = Some(s"producer-test-$index")
        )
      )
    }.toMap

  val layer: RLayer[
    Map[Topic, Producer[Notifier.NotifierPayload]],
    MultiTopicProducer
  ] = ZLayer.scoped {
    for {
      dict <- ZIO.service[Map[Topic, Producer[Notifier.NotifierPayload]]]
      ref <- Ref.make(dict)
    } yield MultiTopicProducer(ref)
  }
