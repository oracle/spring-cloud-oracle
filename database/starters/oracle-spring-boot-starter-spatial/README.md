# Oracle Spring Boot Starter for Oracle Spatial

The Oracle Spring Boot Starter for Oracle Spatial provides idiomatic Spring Boot auto-configuration and helper utilities for working with Oracle Spatial `SDO_GEOMETRY` data using GeoJSON-first APIs.

This starter is focused on geographic and topographic spatial data. It does not provide support for Oracle Database AI `VECTOR` columns or vector similarity search.

The starter contributes:

- `OracleSpatialGeoJsonConverter` for `SDO_UTIL.FROM_GEOJSON` and `SDO_UTIL.TO_GEOJSON`
- `OracleSpatialSqlBuilder` for common Oracle Spatial query predicates
- `OracleSpatialProperties` for default SRID and distance-unit settings

Applications remain responsible for creating spatial tables, populating `USER_SDO_GEOM_METADATA`, and managing `MDSYS.SPATIAL_INDEX_V2` indexes through migrations or setup SQL.

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

## Query Guidance

- Use `SDO_FILTER` for a fast primary spatial filter.
- Use `SDO_RELATE` when you need exact relationship masks such as `ANYINTERACT` or `INSIDE`.
- Use `SDO_WITHIN_DISTANCE` for radius-based filtering.
- Use `SDO_NN` for nearest-neighbor searches.
- Avoid combining `SDO_NN` and `SDO_WITHIN_DISTANCE` in the same simple `WHERE` clause unless you are intentionally building a more advanced Oracle Spatial query pattern.
