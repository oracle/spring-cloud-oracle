# Oracle Spring Boot Sample for JSON Events and the Kafka Java Client for Oracle TxEventQ

This sample application demonstrates how to use the Oracle Spring Boot Starter for Kafka Java Clients with Oracle TxEventQ and the Oracle Spring Starter for JSON Collections with [JSON Relational Duality Views](https://docs.oracle.com/en/database/oracle/oracle-database/23/jsnvu/overview-json-relational-duality-views.html) and [Transactional Event Queues](https://www.oracle.com/database/advanced-queuing/). 

The ampld application demonstrates a JSON document ingestion workflow where weather station sensor data is sent to a backend application, using a TxEventQ Producer, TxEventQ Consumer, and JSON Relational Duality Views:

1. Raw sensor data is sent to a REST endpoint.
2. The raw sensor data is parsed as a POJO and produced to an TxEventQ topic in serialized JSONB format.
3. The TxEventQ consumer receives the sensor POJO, enriching and saving the POJO to the database as a JSON Relational Duality View.
4. After consumption, enriched sensor data can be queried from the database by their weather station ID.

## Run the sample application

The sample application test uses Testcontainers, and creates a temporary Oracle Free container database, and requires a docker runtime environment.

To run application test, run the following command:

```shell
mvn test
```

The test starts a sensor data consumer, and sends a series of raw weather station events to the producer. The test verifies that the events have been processed and saved to the database, available in JSON Relational Duality View form.

## Configure your project to use Oracle JSON Relational Duality Views

To use Kafka Java Client for Oracle TxEventQ and Oracle JSON Relational Duality Views from your Spring Boot application, add the following Maven dependencies to your project:

```xml
<dependency>
    <groupId>com.oracle.database.spring</groupId>
    <artifactId>oracle-spring-boot-starter-json-collections</artifactId>
</dependency>
<dependency>
    <groupId>com.oracle.database.spring</groupId>
    <artifactId>oracle-spring-boot-starter-okafka</artifactId>
</dependency>
```
