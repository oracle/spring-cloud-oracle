# Oracle Spring Boot Starter for Oracle Spatial

The Oracle Spring Boot Starter for Oracle Spatial provides idiomatic Spring Boot auto-configuration and helper utilities for working with Oracle Spatial using GeoJSON-first APIs.

The starter contributes:

- `OracleSpatialGeoJsonConverter` for `SDO_UTIL.FROM_GEOJSON` and `SDO_UTIL.TO_GEOJSON`
- `OracleSpatialSqlBuilder` for common Oracle Spatial query predicates
- `OracleSpatialProperties` for default SRID and distance-unit settings

## Dependency Coordinates

```xml
<dependency>
    <groupId>com.oracle.database.spring</groupId>
    <artifactId>oracle-spring-boot-starter-spatial</artifactId>
</dependency>
```

## Example

Inject the helper beans into a normal Spring JDBC service:

```java
@Service
class LandmarkService {
    private final JdbcClient jdbcClient;
    private final OracleSpatialSqlBuilder sqlBuilder;

    LandmarkService(JdbcClient jdbcClient, OracleSpatialSqlBuilder sqlBuilder) {
        this.jdbcClient = jdbcClient;
        this.sqlBuilder = sqlBuilder;
    }
}
```
