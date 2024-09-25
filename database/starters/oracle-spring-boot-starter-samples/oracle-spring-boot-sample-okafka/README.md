# Oracle Spring Boot Sample for the Kafka Java Client for Oracle Database Transactional Event Queues

With Oracle Database 23ai, powerful Kafka APIs are easily used to read and write data backed by [Oracle Database Transactional Event Queues](https://docs.oracle.com/en/database/oracle/oracle-database/21/adque/aq-introduction.html).

If you’re unfamiliar with Oracle Database Transactional Event Queues, it’s a robust, real-time message broker that runs within Oracle Database, designed for high throughput — Oracle Database Transactional Event Queues can handle approximately [100 billion messages per day](https://www.oracle.com/database/advanced-queuing/) on an 8-node Oracle RAC cluster.

This sample application demonstrates how to use the Oracle Spring Boot Starter for the [Kafka Java Client for Oracle Database Transactional Event Queues](https://github.com/oracle/okafka)

The Spring Boot sample application includes the following components to demonstrate application development using Kafka APIs for Oracle Transactional Event Queues:

- Sample Oracle Database Transactional Event Queues Producers and Consumers
- Connection properties for Oracle Database Transactional Event Queues
- Topic management using the Oracle Database Transactional Event Queues admin client
- Spring Boot configuration for the Kafka Java Client for Oracle Database Transactional Event Queues
- A comprehensive test using Spring Boot that produces and consumes data from Transactional Event Queues using the Kafka Java Client for Oracle Database Transactional Event Queues

## Run the sample application

The sample application test uses Testcontainers, and creates a temporary Oracle Free container database, and requires a docker runtime environment.

To run the test application, run the following command:

```shell
mvn test
```

## Configure your project to use the Kafka Java Client for Oracle Database Transactional Event Queues

To use the Kafka Java Client for Oracle Database Transactional Event Queues from your Spring Boot application, add the following Maven dependency to your project:

```xml
<dependency>
    <groupId>com.oracle.database.spring</groupId>
    <artifactId>oracle-spring-boot-starter-okafka</artifactId>
</dependency>
```
