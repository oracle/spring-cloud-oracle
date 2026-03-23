---
title: Project Overview
sidebar_position: 1
---

# Spring Cloud Oracle

Spring Cloud Oracle is a multi-module project providing Spring Boot integrations for Oracle AI Database, Oracle Cloud Infrastructure, and Oracle AI Database native messaging.

The repository is organized around three subprojects:

## Oracle AI Database Spring Boot Starters

The database starters implement Spring Boot integrations for Oracle AI Database development, focusing on application connectivity, database-native messaging, and modern data features.

- Universal Connection Pool (`UCP`) for Oracle-backed `DataSource` configuration
- Oracle AI Database Wallet support for secure connectivity
- `AQ/JMS` integration for Oracle Advanced Queuing and Transactional Event Queues
- OKafka messaging with Apache Kafka Java APIs
- JSON collections and JSON Relational Duality Views for Oracle AI Database 26ai style data access

## Spring Cloud Stream Binder for Oracle TxEventQ

The TxEventQ stream binder implements the Spring Cloud Stream programming model on top of Oracle AI Database Transactional Event Queues.

This subproject is for teams building event-driven services that want:

- standard Spring Cloud Stream `Supplier`, `Function`, and `Consumer` bindings
- Oracle AI Database-backed messaging semantics
- integration with the same Oracle connection infrastructure used elsewhere in the project

## Spring Cloud OCI

Spring Cloud OCI provides idiomatic Spring integrations for Oracle Cloud Infrastructure services. OCI capabilities are packaged as Spring Boot starters, templates, resources, and configuration mechanisms.

Spring Cloud OCI includes integrations for services such as:

- Autonomous Database
- Vault
- Object Storage
- Streaming
- Queues
- Notifications
- Logging
- Email Delivery
- Functions
- Oracle NoSQL Database
- Generative AI
-
## How They Fit Together

These three subprojects are complementary:

- the database starters cover direct Oracle AI Database integration
- the TxEventQ binder adds Spring Cloud Stream support for Oracle AI Database messaging
- Spring Cloud OCI connects Spring applications to managed OCI services around the database and application runtime

Together, they provide a source of integrations for building Spring applications that span Oracle technologies.

## Contributing

Contributions are welcomed in the form of bug reports, features, documentation, and pull requests. See [CONTRIBUTING.md](https://github.com/oracle/spring-cloud-oracle/blob/main/CONTRIBUTING.md) for details on contributing to this project.