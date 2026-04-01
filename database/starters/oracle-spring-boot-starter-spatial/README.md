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

## Configuration Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `oracle.database.spatial.enabled` | `boolean` | `true` | Enables or disables the spatial auto-configuration |
| `oracle.database.spatial.default-srid` | `int` | `4326` | SRID embedded in generated `SDO_UTIL.FROM_GEOJSON` calls; must be positive |
| `oracle.database.spatial.default-distance-unit` | `String` | `M` | Distance unit token appended to generated distance clauses; Oracle-supported values include `M`, `KM`, and `UNIT=MILE` |

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
- Do not combine `SDO_NN` and `SDO_WITHIN_DISTANCE` in the same `WHERE` clause.
- Use `SDO_WITHIN_DISTANCE` ordered by `SDO_GEOM.SDO_DISTANCE` when you need both a distance bound and a result count.
