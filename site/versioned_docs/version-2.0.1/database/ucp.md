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
