# Oracle Spatial Starter Review Revision 2 Follow-Up Plan

## Purpose

This plan captures the remaining work identified in `oracle-spatial-starter-review.md` after the first hardening pass. The updated review says the starter is close to merge-ready, so this plan is intentionally small and focused on the last cleanup items.

## Goal

- Resolve the three P1 issues before merge.
- Pick up the small P2 and P3 polish items at the same time if they stay low-risk.
- Avoid reopening already-resolved design questions.

## Remaining Issues From Review

### P1 items to address before merge

1. Remove dead `OverrideBeans` code from `OracleSpatialAutoConfigurationTest`.
2. Add a configuration properties table to the starter README.
3. Update seed SQL to use the explicit three-argument `sdo_util.from_geojson(..., null, 4326)` form.

### P2 items worth fixing in the same pass

1. Rename the `findWithin` bind parameter from `geometry` to `refGeometry` for clarity.
2. Tighten the README query guidance so it says not to combine `SDO_NN` and `SDO_WITHIN_DISTANCE` in the same `WHERE` clause.

### P3 polish items

1. Switch the `FilteredClassLoader` test to the class-reference form if practical.
2. Add a short code comment explaining the `0.005` tolerance used with `SDO_GEOM.SDO_DISTANCE`.

## Proposed Workstreams

## 1. Remove dead test code

### Problem

`OracleSpatialAutoConfigurationTest` contains an `OverrideBeans` inner class that is not imported into the test application and is therefore dead code. It duplicates coverage that already exists in `OracleSpatialOverrideAutoConfigurationTest`.

### Plan

- Delete the unused `OverrideBeans` inner class from `OracleSpatialAutoConfigurationTest`.
- Keep the test focused on the default auto-configuration case only.
- Verify the override behavior remains covered solely by `OracleSpatialOverrideAutoConfigurationTest`.

## 2. Make seed SQL consistent with runtime SQL generation

### Problem

The init scripts currently call `sdo_util.from_geojson(...)` without the SRID argument, even though the starter-generated SQL uses the explicit SRID form and the sample metadata declares SRID `4326`.

### Plan

- Update both:
  - `database/starters/oracle-spring-boot-spatial-data-tools/src/test/resources/spatial-init.sql`
  - `database/starters/oracle-spring-boot-starter-samples/oracle-spring-boot-sample-spatial/src/test/resources/init.sql`
- Change every seeded geometry insert to:
  - `sdo_util.from_geojson('<geojson>', null, 4326)`
- Keep the rest of the schema and seed data unchanged.

### Outcome

- Test SQL better reflects the recommended pattern users should copy.
- Seed data becomes consistent with `OracleSpatialGeoJsonConverter#fromGeoJsonSql(...)`.

## 3. Improve README completeness and accuracy

### Problem

The starter README still lacks a configuration properties table, and the current `SDO_NN` guidance is too soft.

### Plan

- Add a `Configuration Properties` section to `database/starters/oracle-spring-boot-starter-spatial/README.md` with:
  - `oracle.database.spatial.enabled`
  - `oracle.database.spatial.default-srid`
  - `oracle.database.spatial.default-distance-unit`
- Include type, default, and short description for each property.
- Update the query guidance wording to say:
  - do not combine `SDO_NN` and `SDO_WITHIN_DISTANCE` in the same `WHERE` clause
  - use `SDO_WITHIN_DISTANCE` for radius filtering
  - use `SDO_NN` for nearest-neighbor queries
  - use `SDO_WITHIN_DISTANCE` ordered by `SDO_GEOM.SDO_DISTANCE` when both a radius and ordering are needed

## 4. Align sample clarity with the `findNear` cleanup

### Problem

`findNear` now uses `refGeometry`, but `findWithin` still uses `geometry` as both the column name and the bind parameter name, which is confusing in sample code.

### Plan

- Update `LandmarkService.findWithin(...)` to use `refGeometry` as the bind name.
- Keep the API contract unchanged; this is an internal SQL readability change only.
- Confirm the sample test still covers the endpoint behavior without requiring API changes.

## 5. Apply low-risk polish if it stays trivial

### Problem

There are two small quality items left that are not important enough to justify a separate pass.

### Plan

- Update `OracleSpatialConditionalAutoConfigurationTest` to use:
  - `new FilteredClassLoader(oracle.jdbc.OracleConnection.class)`
  instead of the string form.
- Add a one-line comment near the `0.005` tolerance in `LandmarkService.findNear(...)` explaining that the value is the tolerance used by `SDO_GEOM.SDO_DISTANCE` for the seeded WGS84 sample data.

## Proposed Sequence

1. Remove dead code from `OracleSpatialAutoConfigurationTest`.
2. Update both init SQL files to include SRID `4326`.
3. Update `LandmarkService.findWithin(...)` bind naming and add the distance tolerance comment in `findNear(...)`.
4. Refresh the starter README with the properties table and stricter `SDO_NN` guidance.
5. Apply the `FilteredClassLoader` cosmetic cleanup.
6. Run focused tests for:
   - spatial data-tools auto-configuration tests
   - spatial data-tools integration tests
   - spatial sample tests

## Expected Files

- `database/starters/oracle-spring-boot-spatial-data-tools/src/test/java/com/oracle/spring/spatial/OracleSpatialAutoConfigurationTest.java`
- `database/starters/oracle-spring-boot-spatial-data-tools/src/test/java/com/oracle/spring/spatial/OracleSpatialConditionalAutoConfigurationTest.java`
- `database/starters/oracle-spring-boot-spatial-data-tools/src/test/resources/spatial-init.sql`
- `database/starters/oracle-spring-boot-starter-samples/oracle-spring-boot-sample-spatial/src/main/java/com/oracle/database/spring/spatial/LandmarkService.java`
- `database/starters/oracle-spring-boot-starter-samples/oracle-spring-boot-sample-spatial/src/test/resources/init.sql`
- `database/starters/oracle-spring-boot-starter-spatial/README.md`

## Risks

- Seed SQL updates could break tests if Oracle interprets the three-argument form differently than expected in this image, so this should be verified with the existing Testcontainers suite.
- README wording changes should stay specific and instructional rather than over-explaining Oracle semantics already covered in the linked docs.

## Suggested Outcome

After this pass, the starter should satisfy the Revision 2 review's P1 issues and most of the remaining polish items, leaving the branch in a merge-ready state.
