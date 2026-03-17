---
title: Queues
sidebar_position: 7
---

# Queues

[OCI Queue](https://docs.oracle.com/en-us/iaas/Content/queue/home.htm) is a managed queueing service for independently processed messages without loss or duplication.

## Dependency Coordinates

### Maven

```xml
<dependency>
  <groupId>com.oracle.cloud.spring</groupId>
  <artifactId>spring-cloud-oci-starter-queue</artifactId>
</dependency>
```

### Gradle

```groovy
dependencies {
    implementation("com.oracle.cloud.spring:spring-cloud-oci-starter-queue")
}
```

## Using Queues

The starter auto-configures a `Queue` bean for queue and message operations.

```java
@Autowired
private Queue queue;

public void createQueue() {
    String queueId = queue.createQueue(
            "my-queue",
            compartmentId,
            deadLetterQueueDeliveryCount,
            retentionInSeconds);
}
```

## Configuration

| Name | Description | Required | Default |
| --- | --- | --- | --- |
| `spring.cloud.oci.queue.enabled` | Enables the OCI Queue APIs | No | `true` |

## Sample

See [`spring-cloud-oci-queue-sample`](https://github.com/oracle/spring-cloud-oracle/tree/main/spring-cloud-oci/spring-cloud-oci-samples/spring-cloud-oci-queue-sample).
