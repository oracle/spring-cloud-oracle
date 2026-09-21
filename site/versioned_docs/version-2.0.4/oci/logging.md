---
title: Logging
sidebar_position: 9
---

# Logging

[OCI Logging](https://docs.oracle.com/en-us/iaas/Content/Logging/home.htm) is a managed service for collecting and searching logs from OCI resources and applications.

## Dependency Coordinates

### Maven

```xml
<dependency>
  <groupId>com.oracle.cloud.spring</groupId>
  <artifactId>spring-cloud-oci-starter-logging</artifactId>
</dependency>
```

### Gradle

```groovy
dependencies {
    implementation("com.oracle.cloud.spring:spring-cloud-oci-starter-logging")
}
```

## Using Logging

The starter auto-configures a `LogService` bean for sending application logs to OCI Logging.

```java
@Autowired
private LogService logService;

public void putLog() {
    PutLogsResponse response = logService.putLog("log-text");
}
```

Set `spring.cloud.oci.logging.logId` in application configuration to target the destination log.

## Configuration

| Name | Description | Required | Default |
| --- | --- | --- | --- |
| `spring.cloud.oci.logging.enabled` | Enables the OCI Logging APIs | No | `true` |

## Sample

See [`spring-cloud-oci-logging-sample`](https://github.com/oracle/spring-cloud-oracle/tree/main/spring-cloud-oci/spring-cloud-oci-samples/spring-cloud-oci-logging-sample).
