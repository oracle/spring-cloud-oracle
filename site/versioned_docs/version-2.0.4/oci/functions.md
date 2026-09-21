---
title: Functions
sidebar_position: 11
---

# Functions

[OCI Functions](https://docs.oracle.com/en-us/iaas/Content/Functions/Concepts/functionsoverview.htm) is a fully managed functions-as-a-service platform. Spring Cloud Oracle provides a client for invoking deployed functions.

## Dependency Coordinates

### Maven

```xml
<dependency>
  <groupId>com.oracle.cloud.spring</groupId>
  <artifactId>spring-cloud-oci-starter-function</artifactId>
</dependency>
```

### Gradle

```groovy
dependencies {
    implementation("com.oracle.cloud.spring:spring-cloud-oci-starter-function")
}
```

## Using Functions

The starter auto-configures a `Function` bean for invoking OCI Functions.

```java
@Autowired
private Function function;

public void invoke() {
    InvokeFunctionResponse response =
            function.invokeFunction(functionOcid, endpoint, mode, requestBody);
}
```

## Configuration

| Name | Description | Required | Default |
| --- | --- | --- | --- |
| `spring.cloud.oci.function.enabled` | Enables the OCI Functions APIs | No | `true` |

## Sample

See [`spring-cloud-oci-function-sample`](https://github.com/oracle/spring-cloud-oracle/tree/main/spring-cloud-oci/spring-cloud-oci-samples/spring-cloud-oci-function-sample).
