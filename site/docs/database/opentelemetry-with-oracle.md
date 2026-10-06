---
title: OpenTelemetry with Oracle AI Database
description: Instrument Oracle JDBC calls with OpenTelemetry in Spring Boot applications using the Oracle OpenTelemetry starter.
keywords:
  - Oracle JDBC OpenTelemetry
  - Spring Boot database tracing
  - Oracle AI Database observability
sidebar_position: 6
---

# OpenTelemetry with Oracle AI Database

The `spring-boot-starter-oracle-otel` starter instruments Oracle JDBC activity in Spring Boot applications with OpenTelemetry. It uses the Oracle JDBC observability provider, which can export driver events to OpenTelemetry and Java Flight Recorder (JFR).

Use it when you want traces to flow from an incoming Spring Boot HTTP request into Oracle Database JDBC operations so those spans can be exported to an OpenTelemetry backend such as Grafana LGTM or Zipkin-compatible tooling.

## Dependency Coordinates

```xml
<dependency>
  <groupId>com.oracle.database.spring</groupId>
  <artifactId>spring-boot-starter-oracle-otel</artifactId>
</dependency>
```

## Database Driver Tracing

This starter is intended to support Oracle JDBC OpenTelemetry instrumentation in a Spring Boot application. The sample application demonstrates the typical flow:

- a Spring MVC endpoint receives the request
- Spring Boot tracing creates the application span
- Oracle JDBC OpenTelemetry instrumentation contributes database spans for JDBC work

## Enable the database tracing provider

In your application properties, enable JMX beans for provider management and set the `oracle.jdbc.provider.traceEventListener` JDBC connection URL property to `observability-trace-event-listener-provider`:

```yaml
spring:
  jmx:
    enabled: true
  datasource:
    # Docker compose Oracle Free container
    url: jdbc:oracle:thin:@localhost:1522/freepdb1?oracle.jdbc.provider.traceEventListener=observability-trace-event-listener-provider
```

## Configuration

The provider supports OpenTelemetry and JFR tracers. Configure the enabled tracers with the `oracle.jdbc.provider.observability.enabledTracers` system property. For example, to enable both tracers:

```shell
java -Doracle.jdbc.provider.observability.enabledTracers=OTEL,JFR -jar app.jar
```

Sensitive attributes such as SQL text are disabled by default. To enable them, set `oracle.jdbc.provider.observability.sensitiveDataEnabled=true`. Review your data handling requirements before enabling sensitive attributes.

The provider emits legacy Oracle JDBC semantic conventions by default. To emit the stable OpenTelemetry database conventions, set `OTEL_SEMCONV_STABILITY_OPT_IN=database`. Use `database/dup` to emit both legacy and stable attributes during a migration.

The previous `open-telemetry-trace-event-listener-provider` name and these settings remain available for compatibility: `oracle.jdbc.provider.opentelemetry.enabled` and `oracle.jdbc.provider.opentelemetry.sensitive-enabled`. When the legacy provider name or settings are used, only the OpenTelemetry tracer is enabled.

When tracing is configured in the application, a request that performs JDBC work can be viewed as a single trace spanning the HTTP layer and the database layer.

## Learn by Example

See the sample application:

- [oracle-spring-boot-sample-otel](https://github.com/oracle/spring-cloud-oracle/tree/main/database/starters/oracle-spring-boot-starter-samples/oracle-spring-boot-sample-otel)

## References

- [Spring Boot tracing](https://docs.spring.io/spring-boot/reference/actuator/tracing.html)
- [OJDBC observability provider](https://github.com/oracle/ojdbc-extensions/tree/main/ojdbc-provider-observability)
