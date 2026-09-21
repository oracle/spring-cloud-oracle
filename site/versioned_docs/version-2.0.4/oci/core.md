---
title: OCI Core
sidebar_position: 1
---

# OCI Core

Spring Cloud Oracle provides a core starter that auto-configures OCI authentication and shared infrastructure for the service-specific starters.

## Dependency Coordinates

### Maven

```xml
<dependency>
  <groupId>com.oracle.cloud.spring</groupId>
  <artifactId>spring-cloud-oci-starter</artifactId>
</dependency>
```

### Gradle

```groovy
dependencies {
    implementation("com.oracle.cloud.spring:spring-cloud-oci-starter")
}
```

## Authentication Configuration

Spring Cloud Oracle supports multiple OCI authentication strategies.

### Configuration File Based Authentication

Configuration-file authentication is enabled by default. To set it explicitly:

```properties
spring.cloud.oci.config.type=FILE
spring.cloud.oci.config.profile=DEFAULT
spring.cloud.oci.config.file=<FILE_PATH>
```

For details about the OCI config file format, see the [SDK and CLI configuration file documentation](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/sdkconfig.htm).

### Simple Authentication

Use `SIMPLE` when you want to provide credentials directly via Spring configuration:

```properties
spring.cloud.oci.config.type=SIMPLE
spring.cloud.oci.config.userId=ocid1.user.oc1..<unique_ID>
spring.cloud.oci.config.tenantId=ocid1.tenancy.oc1..<unique_ID>
spring.cloud.oci.config.fingerprint=<FINGERPRINT>
spring.cloud.oci.config.privateKey=<PRIVATE_KEY_FILE_PATH>
spring.cloud.oci.config.passPhrase=<PASS_PHRASE>
spring.cloud.oci.config.region=<REGION>
```

All properties are required except `spring.cloud.oci.config.passPhrase`.

### Instance Principal

```properties
spring.cloud.oci.config.type=INSTANCE_PRINCIPAL
spring.cloud.oci.config.federation-endpoint=https://auth.us-ashburn-1.oraclecloud.com
```

See [OCI Instance Principal Authentication](https://docs.oracle.com/en-us/iaas/Content/Identity/Tasks/callingservicesfrominstances.htm).

### Resource Principal

```properties
spring.cloud.oci.config.type=RESOURCE_PRINCIPAL
spring.cloud.oci.config.federation-endpoint=https://auth.us-ashburn-1.oraclecloud.com
```

See [OCI Resource Principal Authentication](https://docs.public.oneportal.content.oci.oraclecloud.com/en-us/iaas/Content/API/Concepts/sdk_authentication_methods.htm#sdk_authentication_methods_resource_principal).

### Session Token

```properties
spring.cloud.oci.config.type=SESSION_TOKEN
```

See the [OCI session token authentication reference](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/sdk_authentication_methods.htm#ariaid-title12).

### Workload Identity

```properties
spring.cloud.oci.config.type=WORKLOAD_IDENTITY
spring.cloud.oci.config.federation-endpoint=https://auth.us-ashburn-1.oraclecloud.com
```

See [OKE Workload Identity Authentication](https://docs.oracle.com/en-us/iaas/Content/ContEng/Tasks/contenggrantingworkloadaccesstoresources.htm).

## Region Configuration

To set a region for the entire application:

```properties
spring.cloud.oci.region.static=us-ashburn-1
```

`spring.cloud.oci.region.static` takes precedence over `spring.cloud.oci.config.region` and the region in the OCI auth configuration file.

## Compartment Configuration

To define a default compartment for OCI operations:

```properties
spring.cloud.oci.compartment.static=<COMPARTMENT_OCID>
```

### Starters

| Starter | Description | Artifact |
| --- | --- | --- |
| Core | Auto-configures OCI authentication and shared infrastructure | `com.oracle.cloud.spring:spring-cloud-oci-starter` |
| Object Storage | Integrates OCI Object Storage with Spring storage and resource APIs | `com.oracle.cloud.spring:spring-cloud-oci-starter-storage` |
| Autonomous Database | Provides access to OCI Autonomous Database lifecycle operations | `com.oracle.cloud.spring:spring-cloud-oci-starter-adb` |
| Vault | Exposes OCI Vault as a Spring property source and API client | `com.oracle.cloud.spring:spring-cloud-oci-starter-vault` |
| Streaming | Integrates OCI Streaming for producing and consuming records | `com.oracle.cloud.spring:spring-cloud-oci-starter-streaming` |
| Queue | Integrates OCI Queue APIs for queue and message operations | `com.oracle.cloud.spring:spring-cloud-oci-starter-queue` |
| Notifications | Publishes notifications and manages topics and subscriptions | `com.oracle.cloud.spring:spring-cloud-oci-starter-notification` |
| Logging | Sends application logs to OCI Logging | `com.oracle.cloud.spring:spring-cloud-oci-starter-logging` |
| Email Delivery | Implements Spring mail integration on OCI Email Delivery | `com.oracle.cloud.spring:spring-cloud-oci-starter-email` |
| Functions | Invokes OCI Functions from Spring applications | `com.oracle.cloud.spring:spring-cloud-oci-starter-function` |
| Oracle NoSQL Database | Adds Spring Data style support for Oracle NoSQL Database | `com.oracle.cloud.spring:spring-cloud-oci-starter-nosql` |
| Generative AI | Exposes OCI Generative AI chat and embedding models | `com.oracle.cloud.spring:spring-cloud-oci-starter-gen-ai` |

## Learn by Example

The repository includes sample applications for the most important integrations:

| Integration         | Sample                                                                                                                                                                           |
|---------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Object Storage      | [`spring-cloud-oci-storage-sample`](https://github.com/oracle/spring-cloud-oracle/tree/main/spring-cloud-oci/spring-cloud-oci-samples/spring-cloud-oci-storage-sample)           |
| Notifications       | [`spring-cloud-oci-notification-sample`](https://github.com/oracle/spring-cloud-oracle/tree/main/spring-cloud-oci/spring-cloud-oci-samples/spring-cloud-oci-notification-sample) |
| Generative AI       | [`spring-cloud-oci-gen-ai-sample`](https://github.com/oracle/spring-cloud-oracle/tree/main/spring-cloud-oci/spring-cloud-oci-samples/spring-cloud-oci-gen-ai-sample)             |
| Logging             | [`spring-cloud-oci-logging-sample`](https://github.com/oracle/spring-cloud-oracle/tree/main/spring-cloud-oci/spring-cloud-oci-samples/spring-cloud-oci-logging-sample)           |
| Streaming           | [`spring-cloud-oci-streaming-sample`](https://github.com/oracle/spring-cloud-oracle/tree/main/spring-cloud-oci/spring-cloud-oci-samples/spring-cloud-oci-streaming-sample)       |
| Functions           | [`spring-cloud-oci-function-sample`](https://github.com/oracle/spring-cloud-oracle/tree/main/spring-cloud-oci/spring-cloud-oci-samples/spring-cloud-oci-function-sample)         |
| Queue               | [`spring-cloud-oci-queue-sample`](https://github.com/oracle/spring-cloud-oracle/tree/main/spring-cloud-oci/spring-cloud-oci-samples/spring-cloud-oci-queue-sample)               |
| Autonomous Database | [`spring-cloud-oci-adb-sample`](https://github.com/oracle/spring-cloud-oracle/tree/main/spring-cloud-oci/spring-cloud-oci-samples/spring-cloud-oci-adb-sample)                   |
| Vault               | [`spring-cloud-oci-vault-sample`](https://github.com/oracle/spring-cloud-oracle/tree/main/spring-cloud-oci/spring-cloud-oci-samples/spring-cloud-oci-vault-sample)               |

Each sample is intended to be deployable and to show both dependency setup and API usage in context.