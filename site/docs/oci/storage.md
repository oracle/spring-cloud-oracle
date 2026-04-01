---
title: Object Storage
sidebar_position: 4
---

# Object Storage

[OCI Object Storage](https://www.oracle.com/cloud/storage/) stores arbitrary files and objects. Spring Cloud Oracle adds both a storage API and a Spring `Resource` integration.

## Dependency Coordinates

### Maven

```xml
<dependency>
  <groupId>com.oracle.cloud.spring</groupId>
  <artifactId>spring-cloud-oci-starter-storage</artifactId>
</dependency>
```

### Gradle

```groovy
dependencies {
    implementation("com.oracle.cloud.spring:spring-cloud-oci-starter-storage")
}
```

## Using Object Storage

The starter auto-configures a `Storage` bean for bucket and object operations.

```java
@Autowired
private Storage storage;

public void createBucketAndUploadFile() {
    Bucket bucket = storage.createBucket("my-bucket");

    storage.upload(
            "my-bucket",
            "my-file.txt",
            inputStream,
            StorageObjectMetadata.builder().contentType("text/plain").build());
}
```

## Spring Resource Support

Object Storage objects can be accessed using Spring's resource abstraction.

```java
@Value("https://objectstorage.us-chicago-1.oraclecloud.com/n/${OCI_NAMESPACE}/b/${OCI_BUCKET}/o/${OCI_OBJECT}")
private Resource myObjectResource;
```

```java
Resource objectResource = applicationContext.getResource(
        "https://objectstorage.us-chicago-1.oraclecloud.com/n/${OCI_NAMESPACE}/b/${OCI_BUCKET}/o/${OCI_OBJECT}");
```

The resulting `Resource` can be read like other Spring resources.

```java
try (InputStream inputStream = myObjectResource.getInputStream()) {
    String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
}
```

Object Storage resources also implement Spring's `WritableResource`, so they can be used for write operations as well.

```java
@Value("https://objectstorage.${OCI_REGION}.oraclecloud.com/n/${OCI_NAMESPACE}/b/${OCI_BUCKET}/o/${OCI_OBJECT}")
private WritableResource myWritableObjectResource;
```

You can also resolve a writable object resource programmatically from the application context.

```java
WritableResource writableResource = (WritableResource) applicationContext.getResource(
        "https://objectstorage.${OCI_REGION}.oraclecloud.com/n/${OCI_NAMESPACE}/b/${OCI_BUCKET}/o/${OCI_OBJECT}");

try (OutputStream outputStream = writableResource.getOutputStream()) {
    outputStream.write("Hello Object Storage".getBytes(StandardCharsets.UTF_8));
}
```

Data written through `WritableResource#getOutputStream()` is uploaded to OCI Object Storage when the stream is closed.

## Configuration

| Name | Description | Required | Default |
| --- | --- | --- | --- |
| `spring.cloud.oci.storage.enabled` | Enables the OCI Object Storage APIs | No | `true` |

## Learn By Example

See [`spring-cloud-oci-storage-sample`](https://github.com/oracle/spring-cloud-oracle/tree/main/spring-cloud-oci/spring-cloud-oci-samples/spring-cloud-oci-storage-sample).
