---
title: Overview
sidebar_position: 1
---

# Spring Cloud Stream Binder for Oracle TxEventQ

[Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream) is a Java framework designed for building event-driven microservices backed by a scalable, fault-tolerant messaging systems. The Oracle AI Database [Transactional Event Queues (TxEventQ)](https://www.oracle.com/database/advanced-queuing/) stream binder implementation allows developers to leverage Oracle’s database messaging platform within the Spring Cloud Stream ecosystem, all while keeping your data within the converged database.

The TxEventQ stream binder integration combines the power of Oracle’s database-enabled messaging system with Spring Cloud Stream’s functional, event-driven development model. With the TxEventQ stream binder, Spring applications can seamlessly produce and consume messages with Oracle AI Database’s high-performance, transactional messaging system, benefiting from Spring’s abstraction layer and configuration capabilities.

## Key Features of TxEventQ Stream Binder

Oracle Database TxEventQ provides a high-throughput, reliable messaging platform built directly into the database.

- Real-time messaging with multiple publishers, consumers, and topics — all with a simple functional interface.
- High throughput on RAC: 100 billion messages per day were measured on an 8-node Oracle Real Application Clusters (RAC) database.
- Convergence of data: Your messaging platform lives within the database.
- Integration with Spring Cloud Stream makes it easy-to-use, and easy to get started.

## Database Version Support

The stream binder supports database versions from Oracle Database 23ai. For best results, we recommend using the latest patch version of Oracle AI Database 26ai. 
