# Oracle Spring Boot Sample for OKafka

With Oracle Database 23ai, powerful Kafka APIs are easily used to read and write data backed by [Transactional Event Queues (TxEventQ)](https://docs.oracle.com/en/database/oracle/oracle-database/21/adque/aq-introduction.html).

If you’re unfamiliar with TxEventQ, it’s a a robust, real-time message broker that runs within Oracle Database, designed for high throughput — TxEventQ can handle approximately [100 billion messages per day](https://www.oracle.com/database/advanced-queuing/) on an 8-node Oracle RAC cluster.

This sample application demonstrates how to use the Oracle Spring Boot Starter OKafka with the [Kafka Java Client for Oracle Transactional Event Queues](https://github.com/oracle/okafka)

The Spring Boot OKafka sample application includes the following components to demonstrate application development using Kafka APIs for Oracle Transactional Event Queues:

- Sample OKafka Producers and Consumers
- Connection properties for OKafka with Oracle Database
- Topic management using OKafka admin client
- Spring Boot configuration for OKafka
- A comprehensive test using Spring Boot that produces and consumes data from Transactional Event Queues using OKafka

## Run the sample application

The sample application creates a temporary Oracle Free container database, and requires a docker runtime environment.

To run the test application, run the following command:

```shell
mvn test
```

## Configure your project to use OKafka

To use OKafka from your Spring Boot application, add the following Maven dependency to your project:

```xml
<dependency>
    <groupId>com.oracle.database.spring</groupId>
    <artifactId>oracle-spring-boot-starter-okafka</artifactId>
</dependency>
```
