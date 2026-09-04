---
title: Testcontainers
sidebar_position: 7
---

# Testcontainers

The Testcontainers module provides container definitions for testing applications with the official Oracle AI Database Free and Oracle REST Data Services images.

### Supported Images

| Container definition | Supported image | Default tag | Purpose |
| --- | --- | --- | --- |
| `OracleContainer` | `container-registry.oracle.com/database/free` | `latest-lite` | Oracle AI Database Free database tests |
| `ADBContainer` | `container-registry.oracle.com/database/adb-free` | `latest-26ai` | Oracle Autonomous AI Database Free tests |
| `OrdsContainer` | `container-registry.oracle.com/database/ords` | `latest` | Oracle REST Data Services tests |


The Database Starters modules and TxEventQ stream binder module use these container definitions for local testing.


## Dependency Coordinates

```xml
<dependency>
  <groupId>com.oracle.database.spring</groupId>
  <artifactId>oracle-spring-boot-testcontainers</artifactId>
  <scope>test</scope>
</dependency>
```

## Quick Start

See the complete [`OracleContainerTest`](https://github.com/oracle/spring-cloud-oracle/blob/main/database/starters/oracle-spring-boot-testcontainers/src/test/java/com/oracle/database/spring/testcontainers/OracleContainerTest.java) integration test for Oracle AI Database Free usage.

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

## Testing Autonomous AI Database Free

See the complete [`ADBContainerTest`](https://github.com/oracle/spring-cloud-oracle/blob/main/database/starters/oracle-spring-boot-testcontainers/src/test/java/com/oracle/database/spring/testcontainers/ADBContainerTest.java) integration test, which covers database, ORDS, and MongoDB API connections.

`ADBContainer` starts the official Oracle Autonomous AI Database Free image. It defaults to the multi-architecture `latest-26ai` image, the Autonomous Transaction Processing (ATP) workload, and the `MYATP` database name. Select the `ADW` Lakehouse workload or an alphanumeric database name when needed. Set both mandatory passwords before starting the container.

```java
ADBContainer database = new ADBContainer()
        .withDatabaseName("orders2026")
        .withAdminPassword("SecurePass1234")
        .withWalletPassword("WalletPassword1")
        .withWorkloadType(ADBContainer.WorkloadType.ATP);
```

Administrator passwords must be 12-30 characters and include uppercase, lowercase, and numeric characters; they cannot contain `ADMIN`. Wallet passwords must be at least eight characters and include letters plus a number or special character. Use `withArchiveLog(false)` to disable the image's default archive logging.

Unlike `OracleContainer`, `ADBContainer` does not create an application user unless one is configured. Use `withAppUser(...)` to create one after the database starts; it receives `CREATE SESSION` and any roles supplied with `withAppUserRoles(...)`:

```java
ADBContainer database = new ADBContainer()
        .withAdminPassword("SecurePass1234")
        .withWalletPassword("WalletPassword1")
        .withAppUser("APP_USER", "AppPassword1")
        .withAppUserRoles("DWROLE", "SODA_APP");
```

`ADBContainer` automatically requests the image's required `SYS_ADMIN` capability and `/dev/fuse` device. It exposes TLS on port 1521, mTLS on port 1522, ORDS/APEX/Database Actions over HTTPS on port 8443, and the MongoDB API on port 27017 through `getTlsPort()`, `getMtlsPort()`, `getHttpsPort()`, and `getMongoDbApiPort()`.

The image generates a TLS wallet at `/u01/app/oracle/wallets/tls_wallet`. `ADBContainer` is a `JdbcDatabaseContainer`: after startup it copies the wallet to a managed temporary directory, updates `tnsnames.ora` for Testcontainers' mapped ports, and uses that directory for `createConnection("")`:

```java
try (Connection connection = database.createConnection("");
     Statement statement = connection.createStatement();
     ResultSet resultSet = statement.executeQuery("SELECT 1 FROM DUAL")) {
    resultSet.next();
}
```

`getJdbcUrl()` returns a JDBC URL using the database's mTLS service alias, while `getUsername()` and `getPassword()` return the configured JDBC credentials. When an application user is configured, it is used by the standard JDBC operations; call `withUsername("ADMIN")` to select the administrator instead. The managed wallet is removed when the container stops.

For a direct `OracleDataSource`, UCP, or other JDBC client, use `getWallet()` after startup to access the managed wallet directory. Do not close this handle; the container owns its lifecycle:

```java
Path walletDirectory = database.getWallet().getDirectory();
dataSource.setConnectionProperty("oracle.net.tns_admin", walletDirectory.toString());
```

When an independent wallet lifecycle is required, use `copyWalletTo(...)` instead. Its returned wallet handle exposes its directory through `getDirectory()` and removes the copied wallet files when closed:

```java
try (ADBContainer.Wallet wallet = database.copyWalletTo(Files.createTempDirectory("adb-free-wallet-"))) {
    Path walletDirectory = wallet.getDirectory();
    // Configure JDBC with walletDirectory as oracle.net.tns_admin.
}
```

`getMtlsServiceAlias()` and `getTlsServiceAlias()` return the workload-appropriate medium-service aliases. The explicit wallet API remains useful when configuring an `OracleDataSource`, UCP, or another JDBC client that needs its own connection properties.

## Testing ORDS

See the complete [`OrdsContainerIntegrationTest`](https://github.com/oracle/spring-cloud-oracle/blob/main/database/starters/oracle-spring-boot-testcontainers/src/test/java/com/oracle/database/spring/testcontainers/OrdsContainerIntegrationTest.java) for ORDS HTTP, HTTPS, Database API, and MongoDB API coverage.

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
