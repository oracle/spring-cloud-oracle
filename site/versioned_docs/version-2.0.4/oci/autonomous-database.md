---
title: Autonomous Database
sidebar_position: 3
---

# Autonomous Database

[OCI Autonomous Database](https://docs.oracle.com/en/cloud/paas/atp-cloud/index.html) is a managed data service that automates patching, upgrades, tuning, and routine maintenance.

## Dependency Coordinates

### Maven

```xml
<dependency>
  <groupId>com.oracle.cloud.spring</groupId>
  <artifactId>spring-cloud-oci-starter-adb</artifactId>
</dependency>
```

### Gradle

```groovy
dependencies {
    implementation("com.oracle.cloud.spring:spring-cloud-oci-starter-adb")
}
```

## Using Autonomous Database

The starter auto-configures an `AutonomousDb` bean that can create, inspect, start, stop, and delete an Autonomous Database, and generate wallets.

```java
@Autowired
AutonomousDb autonomousDatabase;

public void createAutonomousDatabase() {
    autonomousDatabase.createAutonomousDatabase(
        databaseName, compartmentId, adminPassword, dataStorageSizeInGBs, computeCount);
}

public void getAutonomousDatabase() {
    AutonomousDbDetails response = autonomousDatabase.getAutonomousDatabase(databaseId);
}

public void getAutonomousDatabaseWallet() {
    GenerateAutonomousDatabaseWalletResponse response =
            autonomousDatabase.generateAutonomousDatabaseWallet(databaseId, password);
    InputStream is = response.getInputStream();
    int contentLength = response.getContentLength();
}

public void startAutonomousDatabase() {
    StartAutonomousDatabaseResponse response =
            autonomousDatabase.startAutonomousDatabase(databaseId);
}

public void stopAutonomousDatabase() {
    StopAutonomousDatabaseResponse response =
            autonomousDatabase.stopAutonomousDatabase(databaseId);
}
```

## Configuration

| Name | Description | Required | Default |
| --- | --- | --- | --- |
| `spring.cloud.oci.adb.enabled` | Enables the OCI Autonomous Database APIs | No | `true` |

## Sample

See [`spring-cloud-oci-adb-sample`](https://github.com/oracle/spring-cloud-oracle/tree/main/spring-cloud-oci/spring-cloud-oci-samples/spring-cloud-oci-adb-sample).
