# Spring Boot Oracle Database JDBC Tracing

This example application demonstrates how to instrument Oracle Database JDBC connections from a Spring Boot app context with OpenTelemetry.

## References

- [Spring Boot tracing](https://docs.spring.io/spring-boot/reference/actuator/tracing.html)
- [OJDBC observability provider](https://github.com/oracle/ojdbc-extensions/tree/main/ojdbc-provider-observability)

## Prerequisites

- Java 21+, Maven
- Docker compatible environment with docker-compose

## Setup Oracle Database Free and Grafana LGTM

From this directory, start the Oracle Database Free and Grafana LGTM containers:

```bash
docker compose up -d
```

When the database starts, it runs [grant_permissions.sql](./oracle/grant_permissions.sql), which creates the sample user and table.

## Run the sample

This command starts the Java application:

```bash
mvn spring-boot:run
```

## Create a trace

POST to the app's REST API to create a trace, starting with a span for the HTTP invocation that drops into the JDBC/database layer:

```bash
curl -X POST http://localhost:8080/flavors \
  -H "Content-Type: application/json" \
  -d '{"flavor": "Mint Chocolate Chip"}'
```

## View traces

1. Navigate to the Grafana Tracing UI, using the container URL `http://localhost:3000/a/grafana-exploretraces-app`
2. Click "Traces" to find all traces, or search for a specific trace ID
3. View the trace! You can see HTTP request down to database query from a single trace

## Configure JDBC tracing

The sample selects the `observability-trace-event-listener-provider` through its JDBC URL. For provider options such as enabling JFR, exporting sensitive attributes, or migrating to stable OpenTelemetry database semantic conventions, see the [OpenTelemetry with Oracle AI Database guide](https://oracle.github.io/spring-cloud-oracle/site/docs/database/opentelemetry-with-oracle).
