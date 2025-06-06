# Oracle Database Spring Boot Samples

The Oracle Database Spring Boot Samples module provides a suite of comprehensive Spring Boot sample applications designed to enable developers with example code for application development.

## [Oracle UCP with JPA Sample](./oracle-spring-boot-sample-ucp-jpa/README.md)

The Oracle UCP with JPA sample application demonstrates how to use the Oracle Spring Boot Starter UCP with Spring Data JPA, connecting your Oracle Database with powerful ORM abstractions that facilitate rapid development, all while using the best connection pooling library for Oracle Database with Spring Boot.

## [JSON Relational Duality Views Sample](./oracle-spring-boot-sample-json-duality/README.md)

The JSON Relational Duality Views sample application demonstrates how to use the Oracle Spring Boot Starter JSON Collections with [JSON Relational Duality Views](https://docs.oracle.com/en/database/oracle/oracle-database/23/jsnvu/overview-json-relational-duality-views.html). JSON Relational Duality Views layer the advantages of JSON document-style database over existing relational data structures — Powerful JSON views with full CRUD capabilities can be created on relational database schemas, nesting related data into a single document with unified access.

## [JSON Events Sample](./oracle-spring-boot-sample-json-duality/README.md)

This sample application demonstrates how to develop a JSON event-streaming application using Kafka APIs by combining the Oracle Spring Boot Starter for Kafka Java Clients with Oracle Database Transactional Event Queues and the Oracle Spring Starter for JSON Collections. JSON data is stored and processed using [JSON Relational Duality Views](https://docs.oracle.com/en/database/oracle/oracle-database/23/jsnvu/overview-json-relational-duality-views.html), and events are produced/consumed with [Transactional Event Queues](https://www.oracle.com/database/advanced-queuing/).

## [Kafka Java Client for Oracle Database Transactional Event Queues Sample](./oracle-spring-boot-starter-okafka/README.md)

This sample application demonstrates how to use the Oracle Spring Boot Starter for the [Kafka Java Client for Oracle Database Transactional Event Queues](https://github.com/oracle/okafka)

Using an in-database message broker like Oracle Database Transactional Event Queues eliminates the need for external message brokers, reduces overall network traffic, simplifying your overall application architecture — and the Kafka Java Client for Oracle Database Transactional Event Queues library enables developers to create applications for Oracle Database Transactional Event Queues using familiar Kafka APIs for messaging.

## [Oracle UCP using Wallet](./oracle-spring-boot-sample-wallet)

This sample application demonstrates how to connect to an Autonomous database using the Oracle Spring Boot Starter UCP and Oracle Spring Boot Starter Wallet all while using the best connection pooling library for Oracle Database with Spring Boot.

## [Oracle Transactional Event Queues Producer and Consumer Example](./oracle-spring-boot-starter-sample-txeventqjms/)

This sample demonstrates how to use [Transactional Event Queues (TxEventQ)](https://docs.oracle.com/en/database/oracle/oracle-database/23/adque/aq-introduction.html) and JMS using the Oracle Spring Boot Starter AQJMS. The sample has a Consumer and a Producer application. The Producer application puts messages on a TxEventQ and the Consumer application listens on the TxEventQ using a JMSListener.
