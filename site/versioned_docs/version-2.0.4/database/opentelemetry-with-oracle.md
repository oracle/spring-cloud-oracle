---
title: OpenTelemetry with Oracle AI Database
sidebar_position: 6
---

# OpenTelemetry with Oracle AI Database

The `spring-boot-starter-oracle-otel` starter provides a tools instrumenting Oracle JDBC activity in Spring Boot applications with OpenTelemetry.

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

In your application properties, enable JMX beans (for the tracing provider), and set the `oracle.jdbc.provider.traceEventListener` JDBC connection URL property to `open-telemetry-trace-event-listener-provider` like so:

```yaml
spring:
  jmx:
    enabled: true
  datasource:
    # Docker compose Oracle Free container
    url: jdbc:oracle:thin:@localhost:1522/freepdb1?oracle.jdbc.provider.traceEventListener=open-telemetry-trace-event-listener-provider
```

## Configuration Notes

The Oracle JDBC OpenTelemetry provider supports system properties such as:

- `oracle.jdbc.provider.opentelemetry.enabled` to enable or disable the provider
- `oracle.jdbc.provider.opentelemetry.sensitive-enabled` to control export of sensitive values such as SQL text

When tracing is configured in the application, a request that performs JDBC work can be viewed as a single trace spanning the HTTP layer and the database layer.

## Learn by Example

See the sample application:

- [oracle-spring-boot-sample-otel](https://github.com/oracle/spring-cloud-oracle/tree/main/database/starters/oracle-spring-boot-starter-samples/oracle-spring-boot-sample-otel)

## References

- [Spring Boot tracing](https://docs.spring.io/spring-boot/reference/actuator/tracing.html)
- [OJDBC OpenTelemetry provider](https://github.com/oracle/ojdbc-extensions/tree/main/ojdbc-provider-opentelemetry)
