---
title: Oracle Spatial
sidebar_position: 6
---

The Oracle Spatial starter adds Spring Boot auto-configuration for GeoJSON-first Oracle Spatial development with [`SDO_GEOMETRY`](https://docs.oracle.com/en/database/oracle/oracle-database/23/spatl/sdo_geometry-object-type.html).

This starter is for geographic and topographic spatial data.

## Dependency Coordinates

```xml
<dependency>
  <groupId>com.oracle.database.spring</groupId>
  <artifactId>oracle-spring-boot-starter-spatial</artifactId>
</dependency>
```

## Provided Beans

When Oracle JDBC is on the classpath and a `DataSource` is present, the starter auto-configures:

- `OracleSpatialJdbcOperations`
- `OracleSpatialProperties`

If your application provides its own bean of the same type, the starter backs off and uses your custom bean instead.

## What You Inject vs What You Build

The starter injects one main working bean:

- `OracleSpatialJdbcOperations`
  - the Spring JDBC entry point for spatial work
  - creates GeoJSON-backed bind values
  - creates bindable SQL expressions and predicates
  - provides a `RowMapper<String>` for projected GeoJSON columns
  - applies spatial bind parameters to `JdbcClient.StatementSpec`

Per query, `OracleSpatialJdbcOperations` creates lightweight value objects:

- `SpatialGeometry`
  - a GeoJSON payload plus SRID
- `SpatialExpression`
  - a SQL expression such as `SDO_UTIL.TO_GEOJSON(...)` or `SDO_GEOM.SDO_DISTANCE(...)`
- `SpatialPredicate`
  - a SQL predicate such as `SDO_FILTER(...) = 'TRUE'`
- `SpatialRelationMask`
  - enum values for `SDO_RELATE` masks such as `ANYINTERACT`, `INSIDE`, and `CONTAINS`

These are not Spring beans. They are query parts that keep the spatial SQL fragment and its JDBC bind values together.

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
- `default-distance-unit` is used when distance clauses are generated for `SDO_WITHIN_DISTANCE` and `SDO_GEOM.SDO_DISTANCE`

If you are new to SRIDs, Oracle uses the SRID value to identify the geometry's spatial reference system or coordinate system. [Oracle's coordinate system documentation](https://docs.oracle.com/en/database/oracle/oracle-database/26/spatl/coordinate-systems-spatial-reference-systems.html) and the [`SDO_SRID` section of the `SDO_GEOMETRY` reference](https://docs.oracle.com/en/database/oracle/oracle-database/23/spatl/sdo_geometry-object-type.html) are the best places to start.

## Using the Starter

Inject `OracleSpatialJdbcOperations` into a Spring JDBC service and let it supply the spatial expressions, predicates, and row mapping while your code still owns the full SQL statement:

```java
@Service
class LandmarkService {
    private final JdbcClient jdbcClient;
    private final OracleSpatialJdbcOperations spatial;

    LandmarkService(JdbcClient jdbcClient, OracleSpatialJdbcOperations spatial) {
        this.jdbcClient = jdbcClient;
        this.spatial = spatial;
    }

    Landmark create(Landmark landmark) {
        SpatialGeometry geometry = spatial.geometry(landmark.geometry());
        SpatialExpression insertGeometry = spatial.fromGeoJson(geometry);

        spatial.bind(
                        jdbcClient.sql("insert into landmarks (id, name, geometry) values (:id, "
                                + insertGeometry.expression() + ")"),
                        insertGeometry)
                .param("id", landmark.id())
                .update();

        SpatialExpression projectedGeometry = spatial.toGeoJson("geometry");
        return jdbcClient.sql("select id, "
                        + projectedGeometry.selection("geometry")
                        + " from landmarks where id = :id")
                .param("id", landmark.id())
                .query((rs, rowNum) -> new Landmark(rs.getLong("id"), rs.getString("geometry")))
                .single();
    }
}
```

In this pattern:

- the application boundary stays GeoJSON-first
- the starter keeps Oracle Spatial SQL fragments attached to their JDBC bind values
- `JdbcClient` still owns the statement lifecycle
- schema creation, metadata registration, and spatial index creation remain outside the starter

## `OracleSpatialJdbcOperations`

`OracleSpatialJdbcOperations` is the main API exposed by the starter.

Geometry creation:

- `geometry(String geoJson)`
- `geometry(String geoJson, int srid)`

Expression creation:

- `fromGeoJson(SpatialGeometry geometry)`
  - returns `SDO_UTIL.FROM_GEOJSON(...)`
- `toGeoJson(String geometryColumn)`
  - returns `SDO_UTIL.TO_GEOJSON(...)`
- `nearestNeighborDistance()`
  - returns `SDO_NN_DISTANCE(1)`
- `distance(String geometryColumn, SpatialGeometry geometry, Number tolerance)`
- `distance(String geometryColumn, SpatialGeometry geometry, Number tolerance, String unit)`
  - return `SDO_GEOM.SDO_DISTANCE(...)`

Predicate creation:

- `filter(String geometryColumn, SpatialGeometry geometry)`
  - wraps [`SDO_FILTER`](https://docs.oracle.com/en/database/oracle/oracle-database/26/spatl/sdo_filter.html)
- `relate(String geometryColumn, SpatialGeometry geometry, SpatialRelationMask mask)`
  - wraps [`SDO_RELATE`](https://docs.oracle.com/en/database/oracle/oracle-database/26/spatl/sdo_relate.html)
- `withinDistance(String geometryColumn, SpatialGeometry geometry, Number distance)`
- `withinDistance(String geometryColumn, SpatialGeometry geometry, Number distance, String unit)`
  - wrap [`SDO_WITHIN_DISTANCE`](https://docs.oracle.com/en/database/oracle/oracle-database/26/spatl/sdo_within_distance.html)
- `nearestNeighbor(String geometryColumn, SpatialGeometry geometry, int numResults)`
  - wraps [`SDO_NN`](https://docs.oracle.com/en/database/oracle/oracle-database/26/spatl/sdo_nn.html)

Spring JDBC integration:

- `bind(JdbcClient.StatementSpec statement, SpatialJdbcBindable... parts)`
  - applies bind values from spatial expressions and predicates to a `JdbcClient` statement
- `geoJsonRowMapper(String columnLabel)`
  - returns a `RowMapper<String>` for GeoJSON projections

## Query Patterns

Insert GeoJSON as `SDO_GEOMETRY`:

```java
SpatialGeometry geometry = spatial.geometry(geoJson);
SpatialExpression insertGeometry = spatial.fromGeoJson(geometry);

spatial.bind(
                jdbcClient.sql("insert into landmarks (geometry) values (" + insertGeometry.expression() + ")"),
                insertGeometry)
        .update();
```

Project `SDO_GEOMETRY` back to GeoJSON:

```java
SpatialExpression projectedGeometry = spatial.toGeoJson("geometry");

String geoJson = jdbcClient.sql("select " + projectedGeometry.selection("geometry") + " from landmarks where id = :id")
        .param("id", id)
        .query(spatial.geoJsonRowMapper("geometry"))
        .single();
```

Apply a filter plus exact relationship check:

```java
SpatialGeometry searchGeometry = spatial.geometry(polygonGeoJson);
SpatialPredicate filter = spatial.filter("geometry", searchGeometry);
SpatialPredicate relate = spatial.relate("geometry", searchGeometry, SpatialRelationMask.ANYINTERACT);

spatial.bind(
                jdbcClient.sql("select id from landmarks where "
                        + filter.clause() + " and " + relate.clause()),
                filter, relate)
        .query(Long.class)
        .list();
```

Find nearby rows and order by distance:

```java
SpatialGeometry referenceGeometry = spatial.geometry(pointGeoJson);
SpatialPredicate within = spatial.withinDistance("geometry", referenceGeometry, 2000);
SpatialExpression distance = spatial.distance("geometry", referenceGeometry, 0.005);

spatial.bind(
                jdbcClient.sql("select id, " + distance.selection("distance")
                        + " from landmarks where " + within.clause()
                        + " order by distance fetch first 3 rows only"),
                within, distance)
        .query((rs, rowNum) -> rs.getLong("id"))
        .list();
```

## Usage Notes

- Manage spatial table DDL, `USER_SDO_GEOM_METADATA`, and spatial index creation in your migrations or setup SQL rather than expecting starter beans to create them.
- Use `SDO_FILTER` as a primary filter and `SDO_RELATE` for exact mask-based checks.
- Use `SDO_WITHIN_DISTANCE` for radius filtering and `SDO_NN` for nearest-neighbor searches.
- Do not combine `SDO_NN` and `SDO_WITHIN_DISTANCE` in the same `WHERE` clause.
- Use `SDO_WITHIN_DISTANCE` ordered by `SDO_GEOM.SDO_DISTANCE` when you need both a distance bound and a result count.
- `nearestNeighbor(...)` and `nearestNeighborDistance()` currently assume Oracle operator id `1`. If you need more advanced SQL with multiple `SDO_NN` operators in a single statement, build that query manually.

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
