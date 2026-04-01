# MsgSync: Scalable Notification System

MsgSync is a demonstration of a Pulsar-based notification service built with Scala and ZIO. It enables sending and processing email/push notifications via multi-topic producers and consumers, showcasing effectful, composable messaging in a layered architecture.

## Tools and Versions
- **Scala**: 3.7.3
- **ZIO**: 2.1.22 (core, streams, JSON, config)
- **Apache Pulsar**: Via Pulsar4s 2.12.0.1 & Java Client 4.1.2
- **Avro4s**: 5.0.14 (serialization)
- **Circe**: 0.14.15 (JSON)
- **SBT**: Build tool
- Others: Kyo 1.0-RC1, Kafka Clients 4.1.0

## Quick Start
1. \`sbt compile\`
2. Update \`msg-pulsar.conf\` for Pulsar.
3. \`sbt run\` (runs NotifierDemoApp)
