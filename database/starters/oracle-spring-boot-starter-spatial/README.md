# Oracle Spring Boot Starter for Oracle Spatial

The Oracle Spring Boot Starter for Oracle Spatial provides Spring Boot auto-configuration and Spring JDBC-oriented helpers for working with Oracle Spatial `SDO_GEOMETRY` data using GeoJSON-first APIs.

This starter is focused on geographic and topographic spatial data. It does not provide support for Oracle Database AI `VECTOR` columns or vector similarity search.

The starter contributes:

- `OracleSpatialJdbcOperations` as the main spatial JDBC integration bean
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
| `oracle.database.spatial.default-distance-unit` | `String` | `M` | Distance unit token appended to generated `SDO_WITHIN_DISTANCE` and `SDO_GEOM.SDO_DISTANCE` clauses; Oracle-supported values include `M`, `KM`, and `UNIT=MILE` |

## Example

Inject the helper bean into a Spring JDBC service:

```java
@Service
class LandmarkService {
    private final JdbcClient jdbcClient;
    private final OracleSpatialJdbcOperations spatial;

    LandmarkService(JdbcClient jdbcClient, OracleSpatialJdbcOperations spatial) {
        this.jdbcClient = jdbcClient;
        this.spatial = spatial;
    }
}
```

Typical query flow:

- create a `SpatialGeometry` from GeoJSON
- derive a `SpatialExpression` or `SpatialPredicate`
- build the full SQL statement in `JdbcClient`
- call `spatial.bind(...)` to apply the spatial bind values

## Query Guidance

- Use `SDO_FILTER` for a fast primary spatial filter.
- Use `SDO_RELATE` when you need exact relationship masks such as `ANYINTERACT` or `INSIDE`.
- Use `SDO_WITHIN_DISTANCE` for radius-based filtering.
- Use `SDO_NN` for nearest-neighbor searches.
- Use `OracleSpatialJdbcOperations.geoJsonRowMapper(...)` or a custom `RowMapper` when projecting `SDO_UTIL.TO_GEOJSON(...)`.
- Do not combine `SDO_NN` and `SDO_WITHIN_DISTANCE` in the same `WHERE` clause.
- Use `SDO_WITHIN_DISTANCE` ordered by `SDO_GEOM.SDO_DISTANCE` when you need both a distance bound and a result count.
