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

Spring Boot publishes generic `jdbc.connections.*` metrics for UCP when Actuator is enabled. To export UCP-specific runtime statistics as well, Spring Boot actuator and the UCP micrometer dependencies to your project:

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

The module automatically binds every UCP `PoolDataSource` to the available Micrometer registry. All meters use a `pool` tag containing the configured UCP connection-pool name; when that name is missing or blank, the tag falls back to the Spring `DataSource` bean name.

The `ucp.connections.*` metrics include gauges for current pool state (`active`, `idle`, `pending`, `max`, `min`, `capacity`, and peak values), labeled and abandoned connections. The `created` and `closed` gauges report values for the current pool instance and may reset when the pool restarts. The cumulative `borrowed`, `returned`, and `creation.attempts` values are function counters. Connection acquire and use statistics are function timers: `acquire`, `acquire.failed`, `acquire.total`, and `usage`. The `acquire.average` and `acquire.peak` values are time gauges.

Shard-specific statistics are not exported because shard names can produce unbounded metric-tag cardinality.
