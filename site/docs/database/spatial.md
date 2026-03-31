---
title: Oracle Spatial
sidebar_position: 6
---

The Oracle Spatial starter adds Spring Boot auto-configuration and helper beans for GeoJSON-first Oracle Spatial development with [`SDO_GEOMETRY`](https://docs.oracle.com/en/database/oracle/oracle-database/23/spatl/sdo_geometry-object-type.html).

This starter is for geographic and topographic spatial data.

## Dependency Coordinates

```xml
<dependency>
  <groupId>com.oracle.database.spring</groupId>
  <artifactId>oracle-spring-boot-starter-spatial</artifactId>
</dependency>
```

## Provided Beans

- `OracleSpatialGeoJsonConverter` for [`SDO_UTIL.FROM_GEOJSON`](https://docs.oracle.com/en/database/oracle/oracle-database/26/spatl/sdo_util-from_geojson.html) and [`SDO_UTIL.TO_GEOJSON`](https://docs.oracle.com/en/database/oracle/oracle-database/26/spatl/sdo_util-to_geojson.html)
- `OracleSpatialSqlBuilder` for [`SDO_FILTER`](https://docs.oracle.com/en/database/oracle/oracle-database/26/spatl/sdo_filter.html), [`SDO_RELATE`](https://docs.oracle.com/en/database/oracle/oracle-database/26/spatl/sdo_relate.html), [`SDO_WITHIN_DISTANCE`](https://docs.oracle.com/en/database/oracle/oracle-database/26/spatl/sdo_within_distance.html), and [`SDO_NN`](https://docs.oracle.com/en/database/oracle/oracle-database/26/spatl/sdo_nn.html)
- `OracleSpatialProperties` with the prefix `oracle.database.spatial`

When Oracle JDBC is on the classpath and a `DataSource` is present, the starter auto-configures these beans:

- `OracleSpatialGeoJsonConverter`
- `OracleSpatialSqlBuilder`

`OracleSpatialProperties` is also available for injection through Spring Boot configuration properties binding.

If your application provides its own bean of the same type, the starter backs off and uses your custom bean instead.

## Configuration Properties

```yaml
oracle:
  database:
    spatial:
      enabled: true
      default-srid: 4326
      default-distance-unit: M
```

`default-distance-unit` is intentionally flexible and can be set to Oracle-style unit tokens such as `M`, `KM`, or `UNIT=MILE`.

These properties affect generated SQL directly:

- `default-srid` is used when GeoJSON is converted to `SDO_GEOMETRY`
- `default-distance-unit` is used when distance clauses are generated for `SDO_WITHIN_DISTANCE`

If you are new to SRIDs, Oracle uses the SRID value to identify the geometry's spatial reference system or coordinate system. [Oracle's coordinate system documentation](https://docs.oracle.com/en/database/oracle/oracle-database/26/spatl/coordinate-systems-spatial-reference-systems.html) and the [`SDO_SRID` section of the `SDO_GEOMETRY` reference](https://docs.oracle.com/en/database/oracle/oracle-database/23/spatl/sdo_geometry-object-type.html) are the best places to start.

## Using the Starter

Inject the helper beans into a normal Spring service that uses `JdbcClient` or `JdbcTemplate`:

```java
@Service
class LandmarkService {
    private final JdbcClient jdbcClient;
    private final OracleSpatialGeoJsonConverter geoJsonConverter;
    private final OracleSpatialSqlBuilder sqlBuilder;

    LandmarkService(JdbcClient jdbcClient,
                    OracleSpatialGeoJsonConverter geoJsonConverter,
                    OracleSpatialSqlBuilder sqlBuilder) {
        this.jdbcClient = jdbcClient;
        this.geoJsonConverter = geoJsonConverter;
        this.sqlBuilder = sqlBuilder;
    }

    Landmark create(Landmark landmark) {
        jdbcClient.sql("insert into landmarks (id, name, geometry) values (:id, "
                        + sqlBuilder.insertGeometryExpression("geometry") + ")")
                .param("id", landmark.id())
                .param("geometry", landmark.geometry())
                .update();

        return jdbcClient.sql("select id, "
                        + sqlBuilder.geometryToGeoJson("geometry") + " as geometry "
                        + "from landmarks where id = :id")
                .param("id", landmark.id())
                .query((rs, rowNum) -> new Landmark(rs.getLong("id"), rs.getString("geometry")))
                .single();
    }
}
```

In this pattern:

- the application boundary stays GeoJSON-first
- the starter generates Oracle Spatial SQL fragments
- schema creation, metadata registration, and spatial index creation remain outside the starter

## `OracleSpatialGeoJsonConverter`

`OracleSpatialGeoJsonConverter` is the lower-level helper for SQL fragments related to GeoJSON conversion and distance clause generation.

Available capabilities:

- `fromGeoJsonSql(String bindExpression)`
  - returns `SDO_UTIL.FROM_GEOJSON(...)` using the configured default SRID
- `fromGeoJsonSql(String bindExpression, int srid)`
  - same conversion, but with a per-query SRID override
- `toGeoJsonSql(String geometryExpression)`
  - returns `SDO_UTIL.TO_GEOJSON(...)` for a column or SQL expression
- `distanceClause(Number distance)`
  - returns a distance clause using the configured default distance unit
- `distanceClause(Number distance, String unit)`
  - same clause, but with a per-query unit override
- `defaultSrid()`
  - returns the configured default SRID
- `defaultDistanceUnit()`
  - returns the configured default distance unit

Typical use cases:

- use `fromGeoJsonSql(...)` when building custom SQL not covered by `OracleSpatialSqlBuilder`
- use `toGeoJsonSql(...)` when projecting `SDO_GEOMETRY` values back to GeoJSON
- use `distanceClause(...)` when hand-assembling Oracle Spatial predicates

For background on GeoJSON conversion behavior, see Oracle's documentation for `SDO_UTIL.FROM_GEOJSON` and `SDO_UTIL.TO_GEOJSON`.

## `OracleSpatialSqlBuilder`

`OracleSpatialSqlBuilder` is the higher-level helper for common Oracle Spatial query fragments.

Available capabilities:

- `geometryFromGeoJson(String bindName)`
- `geometryFromGeoJson(String bindName, int srid)`
- `geometryToGeoJson(String geometryExpression)`
- `insertGeometryExpression(String geoJsonBindName)`
- `insertGeometryExpression(String geoJsonBindName, int srid)`
- `filterPredicate(String geometryColumn, String geoJsonBindName)`
- `filterPredicate(String geometryColumn, String geoJsonBindName, int srid)`
- `relatePredicate(String geometryColumn, String geoJsonBindName, String mask)`
- `relatePredicate(String geometryColumn, String geoJsonBindName, String mask, int srid)`
- `withinDistancePredicate(String geometryColumn, String geoJsonBindName, Number distance)`
- `withinDistancePredicate(String geometryColumn, String geoJsonBindName, Number distance, String unit)`
- `nearestNeighborPredicate(String geometryColumn, String geoJsonBindName, int numResults)`
- `nearestNeighborDistanceProjection(String alias)`
- `nearestNeighborDistanceExpression()`

Method guidance:

- `geometryFromGeoJson(...)`
  - use when you need a named bind such as `:geometry` converted into `SDO_GEOMETRY`
- `geometryToGeoJson(...)`
  - use in `SELECT` projections to turn a geometry column back into GeoJSON
- `insertGeometryExpression(...)`
  - use in `INSERT` and `UPDATE` statements
- `filterPredicate(...)`
  - use for a fast primary filter
- `relatePredicate(...)`
  - use for exact relationship masks such as `ANYINTERACT` or `INSIDE`
  - blank masks normalize to `ANYINTERACT`
- `withinDistancePredicate(...)`
  - use for radius-based filtering
- `nearestNeighborPredicate(...)`
  - use for nearest-neighbor queries
- `nearestNeighborDistanceProjection(...)`
  - use in a `SELECT` list when you also need the Oracle nearest-neighbor distance value
- `nearestNeighborDistanceExpression()`
  - use in `ORDER BY`

Note that `nearestNeighborPredicate(...)` and the related distance helpers currently assume Oracle operator id `1`. If you need more advanced SQL with multiple `SDO_NN` operators in a single statement, build that query manually.

## Usage Notes

- Manage spatial table DDL, `USER_SDO_GEOM_METADATA`, and spatial index creation in your migrations or setup SQL rather than expecting starter beans to create them.
- Use `SDO_FILTER` as a primary filter and `SDO_RELATE` for exact mask-based checks.
- Use `SDO_WITHIN_DISTANCE` for radius filtering and `SDO_NN` for nearest-neighbor searches.
- Avoid combining `SDO_NN` and `SDO_WITHIN_DISTANCE` in a simple `WHERE` clause unless you are intentionally building an advanced Oracle Spatial query pattern.

## Further Reading

- [Oracle Spatial Concepts](https://docs.oracle.com/en/database/oracle/oracle-database/26/spatl/spatial-concepts.html)
- [SDO_GEOMETRY Object Type](https://docs.oracle.com/en/database/oracle/oracle-database/23/spatl/sdo_geometry-object-type.html)
- [Coordinate Systems (Spatial Reference Systems)](https://docs.oracle.com/en/database/oracle/oracle-database/26/spatl/coordinate-systems-spatial-reference-systems.html)
- [SDO_SRID in the spatial data types and metadata reference](https://docs.oracle.com/en/database/oracle/oracle-database/21/spatl/spatial-datatypes-metadata.html)
- [SDO_UTIL.FROM_GEOJSON](https://docs.oracle.com/en/database/oracle/oracle-database/26/spatl/sdo_util-from_geojson.html)
- [SDO_UTIL.TO_GEOJSON](https://docs.oracle.com/en/database/oracle/oracle-database/26/spatl/sdo_util-to_geojson.html)
- [SDO_FILTER](https://docs.oracle.com/en/database/oracle/oracle-database/26/spatl/sdo_filter.html)
- [SDO_RELATE](https://docs.oracle.com/en/database/oracle/oracle-database/26/spatl/sdo_relate.html)
- [SDO_WITHIN_DISTANCE](https://docs.oracle.com/en/database/oracle/oracle-database/26/spatl/sdo_within_distance.html)
- [SDO_NN](https://docs.oracle.com/en/database/oracle/oracle-database/26/spatl/sdo_nn.html)
- [SDO_NN_DISTANCE](https://docs.oracle.com/en/database/oracle/oracle-database/26/spatl/sdo_nn_distance.html)

## Sample

See the spatial sample application under `database/starters/oracle-spring-boot-starter-samples/oracle-spring-boot-sample-spatial` for a REST-based example that stores and queries `SDO_GEOMETRY` values using GeoJSON. Its `GET /landmarks/near` endpoint accepts compact GeoJSON in the `geometry` query parameter.
