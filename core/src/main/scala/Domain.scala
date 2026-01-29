package io.msgsync.core

import scala.util.matching.Regex
import com.sksamuel.avro4s.{SchemaFor, Encoder, Decoder}

/*
final case class EmailConfig(
    smtpHost: String,
    smtpPort: Int,
    sender: String
)

final case class PushConfig(
    apiKey: String,
    endpoint: String
)
 */

trait Validated[Self] {
  val regex: Regex
  val typeName: String
  val extraCheck: String => Boolean = _ => true

  inline def apply(value: String): Either[String, Self] =
    if isValid(value) then Right(value.asInstanceOf[Self])
    else Left(s"Invalid ${typeName} format: $value")

  private inline def isValid(value: String): Boolean =
    value != null && extraCheck(value) && regex.matches(value)

  extension (self: Self) {
    def value: String = self.asInstanceOf[String]
  }
}

type EmailAddress = EmailAddress.T
object EmailAddress extends Validated[EmailAddress]:
  opaque type T = String
  val regex: Regex = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".r
  val typeName: String = "email"

  given SchemaFor[EmailAddress] = SchemaFor[String]
  given Encoder[EmailAddress] =
    Encoder[String].contramap[EmailAddress](_.value)
  given Decoder[EmailAddress] = Decoder[String].map[EmailAddress](s =>
    EmailAddress(s).fold(
      err => throw new IllegalArgumentException(s"Invalid email: $s: $err"),
      identity
    )
  )
end EmailAddress

type PushToken = PushToken.T
object PushToken extends Validated[PushToken]:
  opaque type T = String
  val regex: Regex = """^[a-fA-F0-9]{64}|[a-zA-Z0-9_-]{100,}$""".r
  val typeName: String = "push_token"
  override val extraCheck: String => Boolean = _.nonEmpty

  given SchemaFor[PushToken] = SchemaFor[String]
  given Encoder[PushToken] = Encoder[String].contramap[PushToken](_.value)
  given Decoder[PushToken] = Decoder[String].map[PushToken](s =>
    PushToken(s).fold(
      err =>
        throw new IllegalArgumentException(s"Invalid push token: $s: $err"),
      identity
    )
  )
end PushToken

case class PushRecipient(pushToken: PushToken, deviceType: String)
given Encoder[PushRecipient] = Encoder.derived
given Decoder[PushRecipient] = Decoder.derived
given SchemaFor[PushRecipient] = SchemaFor[PushRecipient]
