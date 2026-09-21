---
title: Streaming
sidebar_position: 6
---

# Streaming

[OCI Streaming](https://docs.oracle.com/en-us/iaas/Content/Streaming/home.htm) is a managed service for ingesting and consuming high-volume data streams in real time.

## Dependency Coordinates

### Maven

```xml
<dependency>
  <groupId>com.oracle.cloud.spring</groupId>
  <artifactId>spring-cloud-oci-starter-streaming</artifactId>
</dependency>
```

### Gradle

```groovy
dependencies {
    implementation("com.oracle.cloud.spring:spring-cloud-oci-starter-streaming")
}
```

## Using Streaming

The starter auto-configures a `Streaming` bean for creating streams and sending or receiving messages.

```java
@Autowired
private Streaming streaming;

public void putMessages() {
    PutMessagesResponse response =
            streaming.putMessages(streamId, "key".getBytes(), "value".getBytes());
}

public void getMessages() {
    GetMessagesResponse response = streaming.getMessages(streamId, "cursor");
}
```

## Configuration

| Name | Description | Required | Default |
| --- | --- | --- | --- |
| `spring.cloud.oci.stream.enabled` | Enables the OCI Streaming APIs | No | `true` |

## Sample

See [`spring-cloud-oci-streaming-sample`](https://github.com/oracle/spring-cloud-oracle/tree/main/spring-cloud-oci/spring-cloud-oci-samples/spring-cloud-oci-streaming-sample).
