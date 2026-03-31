# Oracle Spatial Starter v1 Plan

## Summary

- Add a new two-module spatial addition that mirrors the existing JSON pattern:
  - `oracle-spring-boot-spatial-data-tools` for the actual auto-configuration and helper beans
  - `oracle-spring-boot-starter-spatial` as the thin dependency starter
- Add a new sample module: `oracle-spring-boot-sample-spatial`.
- Scope v1 to Oracle-native core vector spatial support only: `SDO_GEOMETRY`, GeoJSON conversion, metadata/index setup through SQL, and helper support for `SDO_FILTER`, `SDO_RELATE`, `SDO_WITHIN_DISTANCE`, and `SDO_NN`.

## Key Changes

- Wire the new modules into `database/starters/pom.xml` and the sample into `oracle-spring-boot-starter-samples/pom.xml`, using the same parent, versions, plugins, and packaging conventions already used across the database starters.
- In `oracle-spring-boot-spatial-data-tools`, add `com.oracle.spring.spatial` as the main package and register auto-configuration through both `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` and `META-INF/spring.factories`.
- Make the data-tools module depend on:
  - `oracle-spring-boot-starter-ucp`
  - `spring-boot-starter-jdbc`
  - `spring-boot-configuration-processor`
  - the same test/testcontainers stack already used by the other starter modules
- Expose these public types:
  - `OracleSpatialAutoConfiguration`
  - `OracleSpatialProperties` with prefix `oracle.database.spatial`
  - `OracleSpatialGeoJsonConverter`
  - `OracleSpatialSqlBuilder`
- Keep the v1 Java API GeoJSON-first and `JdbcClient`-friendly:
  - `OracleSpatialGeoJsonConverter` handles read/write conversion boundaries around `SDO_UTIL.FROM_GEOJSON` and `SDO_UTIL.TO_GEOJSON`
  - `OracleSpatialSqlBuilder` generates the Oracle SQL fragments needed for inserts/projections and the core predicates for `filter`, `relate`, `within-distance`, and nearest-neighbor queries
  - application code continues to use normal Spring `JdbcClient` / `JdbcTemplate` with dependency injection rather than a custom repository framework
- Default properties:
  - `oracle.database.spatial.enabled=true`
  - `oracle.database.spatial.default-srid=4326`
  - `oracle.database.spatial.default-distance-unit=M`
- Auto-configuration conditions:
  - require a `DataSource`
  - require Oracle JDBC classes on the classpath
  - back off on user-defined beans of the same type
- Keep schema and index creation out of the library beans. Spatial table DDL, `USER_SDO_GEOM_METADATA`, and `MDSYS.SPATIAL_INDEX_V2` creation belong in sample SQL and user migrations, not hidden startup behavior.

## Sample

- Add `oracle-spring-boot-sample-spatial` under `oracle-spring-boot-starter-samples`.
- Build it as a small REST app using `spring-boot-starter-web`, `JdbcClient`, and `oracle-spring-boot-starter-spatial`.
- Use a simple domain such as `Landmark` with:
  - `id`
  - `name`
  - `category`
  - `geometry` represented as GeoJSON in the HTTP API
- Implement endpoints that demonstrate the starter clearly:
  - `POST /landmarks` to create a row from GeoJSON
  - `GET /landmarks/{id}` to return GeoJSON
  - `GET /landmarks/near` for nearest-neighbor or within-distance point search
  - `POST /landmarks/within` for polygon containment / area search
- Add SQL init scripts that:
  - create the sample table with an `SDO_GEOMETRY` column
  - populate `USER_SDO_GEOM_METADATA`
  - create the spatial index
  - seed a few rows using GeoJSON conversion
- Keep the sample consistent with existing repo samples:
  - README with dependency coordinates and test command
  - `application.yaml`
  - integration-style verification rather than a purely illustrative skeleton

## Test Plan

- Add a starter auto-configuration test that verifies the spatial beans load when `DataSource` is present and back off correctly when the user provides overrides.
- Add integration tests for the data-tools module using Testcontainers to verify:
  - GeoJSON -> `SDO_GEOMETRY` -> GeoJSON round-trip
  - basic insert/select through injected Spring beans
  - `SDO_FILTER`
  - `SDO_RELATE`
  - `SDO_WITHIN_DISTANCE`
  - `SDO_NN`
- Add a sample `@SpringBootTest` + Testcontainers test that exercises the REST flow end to end against seeded spatial data.
- Keep the test style aligned with the existing sample modules: `@SpringBootTest`, `@DynamicPropertySource`, `OracleContainer`, and SQL initialization.

## Assumptions

- v1 is intentionally not a JPA/Hibernate Spatial starter.
- v1 will not introduce JTS or another external geometry model; the public boundary stays GeoJSON-oriented to keep the starter lightweight and REST-friendly.
- The implementation should follow the existing repo’s module layout and code style, especially the JSON starter split and the current database-starters parent build.
- Automated tests should continue to use the repo’s current Oracle Free Testcontainers approach unless the repository explicitly standardizes on a different 26ai-compatible image later.
