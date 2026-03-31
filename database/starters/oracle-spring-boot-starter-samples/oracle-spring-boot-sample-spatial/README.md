# Oracle Spring Boot Sample for Oracle Spatial

This sample application demonstrates how to use the Oracle Spring Boot Starter for Oracle Spatial with a small REST API that stores and queries GeoJSON against `SDO_GEOMETRY`.

The sample includes:

- A `Landmark` REST API for insert and query flows
- SQL initialization for the spatial table, metadata, and index
- Spatial queries for nearest-neighbor, within-distance, and polygon search
- A Spring Boot integration test that runs against Oracle Free with Testcontainers

## Run the sample application

The sample application test uses Testcontainers, and creates a temporary Oracle Free container database, and requires a docker runtime environment.

To run the test application, run the following command:

```shell
mvn -pl oracle-spring-boot-starter-samples/oracle-spring-boot-sample-spatial -am test -Dtest=SpatialSampleApplicationTest
```

## Configure your project to use Oracle Spatial

To use Oracle Spatial from your Spring Boot application, add the following Maven dependency to your project:

```xml
<dependency>
    <groupId>com.oracle.database.spring</groupId>
    <artifactId>oracle-spring-boot-starter-spatial</artifactId>
</dependency>
```
