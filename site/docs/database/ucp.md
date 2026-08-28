---
title: Universal Connection Pool
sidebar_position: 1
---

# Universal Connection Pool

The UCP starter provides an Oracle AI Database `DataSource` backed by Universal Connection Pool.

## Dependency Coordinates

```xml
<dependency>
  <groupId>com.oracle.database.spring</groupId>
  <artifactId>oracle-spring-boot-starter-ucp</artifactId>
</dependency>
```

## Configuring the Data Source

Configure `spring.datasource` in `application.yaml` and add Spring Data JDBC or Spring Data JPA as needed:

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@//myhost:1521/pdb1
    username: ${USERNAME}
    password: ${PASSWORD}
    driver-class-name: oracle.jdbc.OracleDriver
    type: oracle.ucp.jdbc.PoolDataSourceImpl
    oracleucp: # Any UCP specific connection parameters defined here
      connection-factory-class-name: oracle.jdbc.pool.OracleDataSource
      connection-pool-name: AccountConnectionPool
      initial-pool-size: 15
      min-pool-size: 10
      max-pool-size: 30
```

The `oracleucp` block is optional and can be used to fine-tune the pool configuration with Oracle UCP specific properties.

## Micrometer Metrics

Spring Boot publishes generic `jdbc.connections.*` metrics for UCP when Actuator is enabled. To export UCP-specific runtime statistics as well, add the Spring Boot Actuator and UCP Micrometer dependencies to your project:

```xml
<dependency>
    <groupId>com.oracle.database.spring</groupId>
    <artifactId>oracle-spring-boot-ucp-micrometer</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

The module automatically binds every UCP `PoolDataSource` to the available Micrometer registry. Portable pool-state meters are aligned with the Development connection-pool metrics in [OpenTelemetry Semantic Conventions v1.44.0](https://github.com/open-telemetry/semantic-conventions/blob/v1.44.0/docs/db/database-metrics.md#connection-pools):

| Meter | UCP value | Attributes |
| --- | --- | --- |
| `db.client.connection.count` | Available connections | `db.client.connection.pool.name`, `db.client.connection.state=idle` |
| `db.client.connection.count` | Borrowed connections | `db.client.connection.pool.name`, `db.client.connection.state=used` |
| `db.client.connection.max` | Maximum pool size | `db.client.connection.pool.name` |
| `db.client.connection.idle.min` | Minimum idle connections, when supported by UCP | `db.client.connection.pool.name` |
| `db.client.connection.pending_requests` | Pending connection requests | `db.client.connection.pool.name` |

The pool-name attribute uses a nonblank UCP connection-pool name when configured. Otherwise, it uses the Spring `DataSource` bean name, which keeps auto-configured pools unique within the application. The count, maximum, and minimum meters use the Micrometer `connections` base unit; pending requests use `requests`.

These meters are convention-shaped rather than fully compliant. OpenTelemetry v1.44.0 specifies UpDownCounter instruments, but UCP exposes current snapshots and Micrometer represents portable polled snapshots as gauges. The module does not synthesize state-change deltas.

Oracle UCP statistics that do not have an equivalent portable metric remain vendor extensions with the `pool` tag:

| Meter or group | Meaning |
| --- | --- |
| `ucp.connections`, `.min`, `.capacity`, `.peak` | Total connections, minimum pool size, remaining capacity, and peak connections |
| `ucp.connections.active.average`, `.active.peak` | Average and peak borrowed connections |
| `ucp.connections.labeled`, `.abandoned` | Labeled connections and abandoned connections reclaimed |
| `ucp.connections.created`, `.closed` | Current-pool creation and closure values, which may reset when the pool restarts |
| `ucp.connections.borrowed`, `.returned`, `.creation.attempts` | Cumulative function counters |
| `ucp.connections.acquire.average`, `.acquire.peak` | Connection-acquire time gauges |
| `ucp.connections.acquire`, `.acquire.failed`, `.acquire.total`, `.usage` | Function timers backed by cumulative counts and total times |

The module does not emit `db.client.connection.idle.max` because UCP has no maximum-idle-count setting, or `db.client.connection.timeouts` because failed waits are not necessarily timeouts. It also does not emit the `create_time`, `wait_time`, or `use_time` histograms because UCP provides aggregate values rather than individual duration observations. Operation-level metrics such as `db.client.operation.duration` are outside this pool-statistics binder.

Shard-specific statistics are not exported because shard names can produce unbounded metric-tag cardinality.
