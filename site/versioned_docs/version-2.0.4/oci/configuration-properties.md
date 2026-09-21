---
title: Configuration Properties
sidebar_position: 2
---

# Configuration Properties

This page summarizes the common Spring Cloud Oracle OCI properties and the subset that supports configuration refresh at runtime.

## Common OCI Properties

| Name | Default | Description |
| --- | --- | --- |
| `spring.cloud.oci.config.profile` | `DEFAULT` | Name of the profile in the OCI auth config file |
| `spring.cloud.oci.config.file` | NA | Location of the OCI auth config file |
| `spring.cloud.oci.config.type` | `FILE` | Auth type. Allowed values are `FILE`, `SIMPLE`, `INSTANCE_PRINCIPAL`, `RESOURCE_PRINCIPAL`, `SESSION_TOKEN`, and `WORKLOAD_IDENTITY` |
| `spring.cloud.oci.config.federation-endpoint` | NA | Optional token endpoint for `INSTANCE_PRINCIPAL`, `RESOURCE_PRINCIPAL`, or `WORKLOAD_IDENTITY` |
| `spring.cloud.oci.config.userId` | NA | User OCID for `SIMPLE` auth |
| `spring.cloud.oci.config.tenantId` | NA | Tenancy OCID for `SIMPLE` auth |
| `spring.cloud.oci.config.fingerprint` | NA | Public key fingerprint for `SIMPLE` auth |
| `spring.cloud.oci.config.privateKey` | NA | Private key file path for `SIMPLE` auth |
| `spring.cloud.oci.config.passPhrase` | NA | Passphrase for the private key, if encrypted |
| `spring.cloud.oci.config.region` | NA | OCI region used for authentication |
| `spring.cloud.oci.region.static` | NA | Static region used for API calls, overriding auth-file region |
| `spring.cloud.oci.compartment.static` | NA | Default OCI compartment OCID |
| `spring.cloud.oci.storage.enabled` | `true` | Enables the OCI Object Storage module |
| `spring.cloud.oci.notification.enabled` | `true` | Enables the OCI Notifications module |
| `spring.cloud.oci.logging.enabled` | `true` | Enables the OCI Logging module |

## Configuration Refresh

The following properties support runtime configuration refresh without restarting the application:

| Name | Default |
| --- | --- |
| `spring.cloud.oci.config.profile` | `DEFAULT` |
| `spring.cloud.oci.config.file` | NA |
| `spring.cloud.oci.config.type` | NA |
| `spring.cloud.oci.config.userId` | NA |
| `spring.cloud.oci.config.tenantId` | NA |
| `spring.cloud.oci.config.fingerprint` | NA |
| `spring.cloud.oci.config.privateKey` | NA |
| `spring.cloud.oci.config.passPhrase` | NA |
| `spring.cloud.oci.config.region` | NA |
| `spring.cloud.oci.region.static` | NA |
| `spring.cloud.oci.compartment.static` | NA |
| `spring.cloud.oci.logging.logId` | NA |

Spring Boot Actuator can be used to trigger refresh explicitly.

### Maven

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### Gradle

```groovy
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
}
```

Expose the refresh endpoint:

```properties
management.endpoints.web.exposure.include=refresh
```

After updating the externalized configuration, call `POST /actuator/refresh`.
