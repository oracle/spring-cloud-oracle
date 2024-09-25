# Oracle Spring Boot Sample for the Kafka Java Client for Oracle TxEventQ

With Oracle Database 23ai, powerful Kafka APIs are easily used to read and write data backed by [Transactional Event Queues (TxEventQ)](https://docs.oracle.com/en/database/oracle/oracle-database/21/adque/aq-introduction.html).

If you’re unfamiliar with TxEventQ, it’s a robust, real-time message broker that runs within Oracle Database, designed for high throughput — TxEventQ can handle approximately [100 billion messages per day](https://www.oracle.com/database/advanced-queuing/) on an 8-node Oracle RAC cluster.

This sample application demonstrates how to use the Oracle Spring Boot Starter for the [Kafka Java Client for Oracle TxEventQ](https://github.com/oracle/okafka)

The Spring Boot sample application includes the following components to demonstrate application development using Kafka APIs for Oracle Transactional Event Queues:

- Sample TxEventQ Producers and Consumers
- Connection properties for TxEventQ with Oracle Database
- Topic management using TxEventQ admin client
- Spring Boot configuration for the Kafka Java Client for Oracle TxEventQ
- A comprehensive test using Spring Boot that produces and consumes data from Transactional Event Queues using the Kafka Java Client for Oracle TxEventQ

## Run the sample application

The sample application test uses Testcontainers, and creates a temporary Oracle Free container database, and requires a docker runtime environment.

To run the test application, run the following command:

```shell
mvn test
```

## Configure your project to use the Kafka Java Client for Oracle TxEventQ

To use the Kafka Java Client for Oracle TxEventQ from your Spring Boot application, add the following Maven dependency to your project:

```xml
<dependency>
    <groupId>com.oracle.database.spring</groupId>
    <artifactId>oracle-spring-boot-starter-okafka</artifactId>
</dependency>
```
