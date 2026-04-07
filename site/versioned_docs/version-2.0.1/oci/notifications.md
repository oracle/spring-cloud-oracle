---
title: Notifications
sidebar_position: 8
---

# Notifications

[OCI Notifications](https://www.oracle.com/devops/notifications/) is a pub/sub service for delivering alerts and messages to email, SMS, Functions, Slack, PagerDuty, and custom HTTPS endpoints.

## Dependency Coordinates

### Maven

```xml
<dependency>
  <groupId>com.oracle.cloud.spring</groupId>
  <artifactId>spring-cloud-oci-starter-notification</artifactId>
</dependency>
```

### Gradle

```groovy
dependencies {
    implementation("com.oracle.cloud.spring:spring-cloud-oci-starter-notification")
}
```

## Using Notifications

The starter auto-configures a `Notification` bean for topic, subscription, and publish operations.

```java
@Autowired
private Notification notification;

public void createTopic() {
    CreateTopicResponse response =
            notification.createTopic("my-topic", compartmentOcid);
}
```

## Configuration

| Name | Description | Required | Default |
| --- | --- | --- | --- |
| `spring.cloud.oci.notification.enabled` | Enables the OCI Notifications APIs | No | `true` |

## Sample

See [`spring-cloud-oci-notification-sample`](https://github.com/oracle/spring-cloud-oracle/tree/main/spring-cloud-oci/spring-cloud-oci-samples/spring-cloud-oci-notification-sample).
