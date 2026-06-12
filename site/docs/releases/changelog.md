---
title: Database Changelog
sidebar_position: 1
---

# Release Notes

List of upcoming and historic changes to Spring Cloud Oracle.

#### Next, TBD

#### Database Starters

- Upgraded Spring Boot to 4.1.0 and refreshed related Jackson, Micrometer, Micrometer Tracing, and Maven plugin versions

#### Spring Cloud Stream Binder for TxEventQ

- Upgraded Spring Boot to 4.1.0 and refreshed Spring Cloud Stream, Spring Integration, Spring Framework, Jackson, Oracle JDBC/security/AQ JMS, Oracle Spring Boot starter, and Maven test plugin versions

#### Spring AI Oracle

- Initial release of Spring AI Oracle, tracking upstream Spring AI 2.0.0
- Upgraded Spring AI Oracle to Spring AI 2.0.0 and Spring Boot 4.1.0

#### Spring Cloud OCI

- Upgraded Spring Cloud OCI to Spring Boot 4.1.0 and refreshed related Spring Cloud, Spring Framework, Spring Data, Netty, Jackson, OCI SDK, NoSQL driver, and Surefire patch versions

### 2.0.2, May 26th 2026

#### Database Starters

- Upgraded Spring Boot to 4.0.6
- Scoped the GitHub Actions test workflow to Database Starters changes

#### Spring Cloud Stream Binder for TxEventQ

- Upgraded Spring Boot to 4.0.6, imported the Spring Boot dependency BOM to keep transitive Spring Boot artifacts on the patched version, and refreshed related Spring Framework, Testcontainers, Jackson, and SLF4J patch versions

#### Spring AI Oracle

- Added Spring AI Oracle with model, auto-configuration, and starter modules prepared for Oracle Cloud Infrastructure Generative AI bindings
- Added Spring AI Oracle to the GitHub Actions test workflows and scoped the workflow to Spring AI Oracle changes

#### Spring Cloud OCI

- Upgraded Spring Boot to 4.0.6 and refreshed related Spring Framework, Netty, Jackson, OCI SDK, NoSQL driver, and Springdoc patch versions
- Scoped the GitHub Actions test workflow to Spring Cloud OCI changes
- OCI Functions invocation now resolves the function invoke endpoint from OCI metadata using the function OCID, so callers no longer need to supply an endpoint separately
- OCI GenAI module deprecated in favor of Spring AI
- OCI GenAI documentation now clarifies that `ChatModel` requests are stateless and that multi-turn context must be supplied by the application

### 2.0.1, April 14th 2026

#### Database Starters

- Upgrade third-party dependencies
- OpenTelemetry with Oracle AI Database documentation:
  - Added starter usage and configuration guidance for `spring-boot-starter-oracle-otel`
  - Documented how Oracle JDBC tracing spans flow into OpenTelemetry backends
- Oracle Spatial documentation and sample:
  - Added starter documentation for GeoJSON-first `SDO_GEOMETRY` development
  - Added the spatial sample reference covering the REST-based landmarks example

#### Spring Cloud Stream Binder for TxEventQ

- Upgrade third-party dependencies

#### Spring Cloud OCI

- Upgrade third-party dependencies
- OCI Object Storage `WritableResource` support:
  - The OCI Object Storage Spring `Resource` now also implements `WritableResource`, allowing object uploads through `getOutputStream()`
  - The Object Storage sample now demonstrates Spring `Resource`, `WritableResource`, and direct `Storage` API round trips for objects and bucket lifecycle operations
  - The Object Storage documentation now includes `WritableResource` usage in addition to the existing `Resource` examples

### 2.0.0, March 31st

The 2.0.0 adds support for Spring Boot 4 and Spring Framework 7, and includes numerous third party dependency updates. 

Notably, the documentation has been updated and moved to a new site. For historic documentation in the 1.x release line, review documentation from prior releases on GitHub. 

#### Database Starters

- Upgrade third-party dependencies and migrated to Spring Boot 4

#### Spring Cloud Stream Binder for TxEventQ

- Upgrade third-party dependencies and migrated to Spring Boot 4

#### Spring Cloud OCI

- Upgrade third-party dependencies and migrated to Spring Boot 4
