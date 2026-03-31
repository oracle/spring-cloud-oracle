---
title: Oracle Spatial
sidebar_position: 6
---

The Oracle Spatial starter adds Spring Boot auto-configuration and helper beans for GeoJSON-first Oracle Spatial development.

## Dependency Coordinates

```xml
<dependency>
  <groupId>com.oracle.database.spring</groupId>
  <artifactId>oracle-spring-boot-starter-spatial</artifactId>
</dependency>
```

## Provided Beans

- `OracleSpatialGeoJsonConverter` for `SDO_UTIL.FROM_GEOJSON` and `SDO_UTIL.TO_GEOJSON`
- `OracleSpatialSqlBuilder` for `SDO_FILTER`, `SDO_RELATE`, `SDO_WITHIN_DISTANCE`, and `SDO_NN`
- `OracleSpatialProperties` with the prefix `oracle.database.spatial`

## Configuration Properties

```yaml
oracle:
  database:
    spatial:
      enabled: true
      default-srid: 4326
      default-distance-unit: M
```

## Sample

See the spatial sample application under `database/starters/oracle-spring-boot-starter-samples/oracle-spring-boot-sample-spatial` for a REST-based example that stores and queries `SDO_GEOMETRY` values using GeoJSON.
