# Oracle Spring Boot Sample for Oracle Spatial

This sample application demonstrates how to use the Oracle Spring Boot Starter for Oracle Spatial with a small REST API that stores and queries GeoJSON against `SDO_GEOMETRY`.

The sample is intentionally focused on Oracle Spatial geometric data, not Oracle AI `VECTOR` search.

The sample includes:

- A `Landmark` REST API for insert and query flows
- SQL initialization for the spatial table, metadata, and index
- Spatial queries for nearest-neighbor, within-distance, and polygon search
- A Spring Boot integration test that runs against Oracle Free with Testcontainers

The sample keeps spatial schema setup in SQL initialization scripts. In a production application, table DDL, `USER_SDO_GEOM_METADATA`, and spatial index creation should typically live in your migration tooling.

## Run the sample application

The sample application test uses Testcontainers, and creates a temporary Oracle Free container database, and requires a docker runtime environment.

To run the test application, run the following command:

```shell
mvn -pl oracle-spring-boot-starter-samples/oracle-spring-boot-sample-spatial -am test -Dtest=SpatialSampleApplicationTest
```

## Sample API Notes

- `GET /landmarks/near` accepts the `geometry` query parameter as compact GeoJSON on a single line.
- The sample does not add controller validation annotations; production applications should validate request payloads and query parameters before constructing spatial SQL.

## Configure your project to use Oracle Spatial

To use Oracle Spatial from your Spring Boot application, add the following Maven dependency to your project:

```xml
<dependency>
    <groupId>com.oracle.database.spring</groupId>
    <artifactId>oracle-spring-boot-starter-spatial</artifactId>
</dependency>
```
