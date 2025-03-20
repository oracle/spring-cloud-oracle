# Spring Cloud Stream Binder for Oracle Database Transactional Event Queues Sample

This sample application demonstrates how to use the Spring Cloud Stream Binder for Oracle Database Transactional Event Queues in a simple Spring Boot Application.

Spring Cloud Stream exposes a [functional messaging API](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/producing-and-consuming-messages.html) for producing and consuming messages. In this sample we implement three functional interfaces to produce a series of words, capitalize them, and output them.

### WordSupplier: Message Producer

The [WordSupplier](src/main/java/com/oracle/database/spring/cloud/stream/binder/sample/WordSupplier.java) class produces a series of words to a topic. Consumers may subscribe to this topic to review messages from the supplier.

### toUpperCase and stdoutConsumer

Messages from the WordSupplier are piped through the [toUpperCase](src/main/java/com/oracle/database/spring/cloud/stream/binder/sample/StreamConfiguration.java) functional interface to demonstrate stream processing. Finally, each message is consumed and printed to stdout by the `stdoutConsumer`.

### Running the tests

The tests require a docker runtime environment, and will instantiate a local Oracle Database.

To run the tests, use the following command:

```shell
mvn test
```

As the test runs, you should see the following output, indicating messages are being processed by the Oracle Database Transactional Event Queues stream binder:

```
Consumed: SPRING
Consumed: CLOUD
Consumed: STREAM
Consumed: SIMPLIFIES
Consumed: EVENT-DRIVEN
Consumed: MICROSERVICES
Consumed: WITH
Consumed: POWERFUL
Consumed: MESSAGING
Consumed: CAPABILITIES.
```