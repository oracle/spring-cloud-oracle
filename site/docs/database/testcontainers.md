---
title: Testcontainers
sidebar_position: 7
---

# Testcontainers

The Testcontainers module provides container definitions for testing applications with the official Oracle AI Database Free and Oracle REST Data Services images.

Database Starters and TxEventQ stream binder integration tests use these shared definitions instead of maintaining separate Oracle AI Database container configurations in each module.

## Dependency Coordinates

```xml
<dependency>
  <groupId>com.oracle.database.spring</groupId>
  <artifactId>oracle-spring-boot-testcontainers</artifactId>
  <scope>test</scope>
</dependency>
```

## Quick Start

Declare the container as a static JUnit Jupiter field so Testcontainers manages its lifecycle:

```java
@Testcontainers
class DatabaseTest {

    @Container
    static final OracleContainer database =
            new OracleContainer().withInitScript("schema.sql");
}
```

By default, `OracleContainer` uses `container-registry.oracle.com/database/free:latest-lite`, connects to the `FREEPDB1` service on port 1521, and provisions a `TEST` application user. Initialization scripts run with that application user's credentials, so unqualified objects are created in its schema.

### Application User Roles

The application user receives `CREATE SESSION` and `DB_DEVELOPER_ROLE` by default. Replace the default role with one or more database roles when a test needs a different privilege set:

```java
OracleContainer database = new OracleContainer()
        .withAppUserRoles("CONNECT", "RESOURCE");
```

Role names are case-insensitive, must be valid unquoted Oracle AI Database identifiers, and are deduplicated. Calling `withAppUserRoles()` without arguments grants only `CREATE SESSION`; initialization scripts that create database objects will then need additional privileges.

### SID Connections

Use `usingSid()` to connect to the `FREE` container database as `SYSTEM`. In SID mode, `withPassword(...)` configures the administrator password used both by the image and the JDBC connection:

```java
OracleContainer database = new OracleContainer()
        .usingSid()
        .withPassword("SidPassword1");
```

Calling `withUsername(...)` afterward returns the container to the `FREEPDB1` service connection mode. Calling `usingSid()` last selects the `FREE` SID and `SYSTEM` administrator credentials.

## Testing ORDS

`OrdsContainer` runs the official Oracle REST Data Services image alongside an Oracle AI Database container. Put both containers on a shared network and give the database a network alias that is used in the ORDS connection strings:

```java
Network network = Network.newNetwork();

OracleContainer database = new OracleContainer(OracleContainer.IMAGE_NAME + ":latest")
        .withNetwork(network)
        .withNetworkAliases("ordsdb");

OrdsContainer ords = new OrdsContainer()
        .withNetwork(network)
        .withDatabaseConnectionString("jdbc:oracle:thin:@ordsdb:1521/FREEPDB1")
        .withOraclePassword(OracleContainer.DEFAULT_PASSWORD)
        .withSchema("appuser", "appuserpwd", "ordsdb:1521/FREEPDB1");
```

Start the database before ORDS. Any schema passed to `withSchema` must already exist; `OrdsContainer` enables it after ORDS becomes ready. The container exposes mapped HTTP, HTTPS, and MongoDB API ports through `getHttpPort()`, `getHttpsPort()`, and `getMongoDbApiPort()`.

`withSchema(...)` supports passwords containing characters such as `/`, `@`, and spaces. Schema names must be valid unquoted Oracle AI Database identifiers, connect descriptors must be single-line values, and passwords cannot contain double quotes or line breaks.

ORDS installation requires the full Oracle AI Database Free image. Use the `latest` tag, as shown above, rather than the reduced `latest-lite` default.
