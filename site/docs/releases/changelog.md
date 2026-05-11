---
title: Database Changelog
sidebar_position: 1
---

# Release Notes

List of upcoming and historic changes to Spring Cloud Oracle.

### 2.0.2, TBD

#### Spring AI Oracle

- Added Spring AI Oracle with model, auto-configuration, and starter modules prepared for Oracle Cloud Infrastructure Generative AI bindings

#### Spring Cloud OCI

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
