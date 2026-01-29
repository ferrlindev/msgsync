package io.msgsync.core

enum NotificationError(msg: String) extends RuntimeException(msg):
  case EmailError(msg: String) extends NotificationError(msg)
  case PushError(msg: String) extends NotificationError(msg)
  case InvalidRecipient(msg: String) extends NotificationError(msg)
  case ServiceUnavailable(msg: String) extends NotificationError(msg)
  case ConfigurationError(msg: String) extends NotificationError(msg)
