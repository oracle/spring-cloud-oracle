# Oracle Spatial Starter — Code Review (Revision 2)
**Branch:** `spatial-starter`
**Reviewer:** Senior Architect Review
**Date:** 2026-03-31
**Previous review:** 2026-03-31 (Revision 1)

---

## Summary of Changes Made

The developer addressed the previous review thoroughly. Every Priority 0 and Priority 1 item was handled, and all three Priority 2 items were also resolved. The implementation is materially better. What follows is an updated assessment that credits those improvements and identifies the smaller number of issues that remain.

---

## What Was Fixed

| Previous recommendation | Resolution |
|------------------------|------------|
| P0: `findNear` incorrectly combined `SDO_NN` + `SDO_WITHIN_DISTANCE` | Fixed — now uses `SDO_WITHIN_DISTANCE` for filtering and `SDO_GEOM.SDO_DISTANCE` for ordering |
| P1: Javadoc on all `OracleSpatialSqlBuilder` public methods | Done — each method documents the SQL it generates, parameter expectations, and the `mask` default |
| P1: Field-level Javadoc on `OracleSpatialProperties` | Done — class and all three fields documented |
| P1: `FilteredClassLoader` test for `@ConditionalOnClass` | Done — new `OracleSpatialConditionalAutoConfigurationTest` |
| P1: Test for absent `DataSource` | Done — same new test class |
| P1: `SDO_RELATE` mask test | Done — `blankRelateMaskDefaultsToAnyInteract` integration test |
| P1: README disambiguation: `SDO_GEOMETRY` vs Oracle 23ai `VECTOR` | Done in both READMEs |
| P2: Per-query unit override on `withinDistancePredicate` | Done — overloaded with `String unit` |
| P2: Per-query SRID override on `fromGeoJsonSql` and predicates | Done — overloaded with `int srid` |
| P2: Validate `defaultDistanceUnit` | Done — `Assert.hasText`, trim, no-quote guard in setter |
| P2: Rename bind param in `findNear` to avoid shadowing column name | Done — now `refGeometry` |

---

## Detailed Assessment of Updated Code

### `OracleSpatialProperties` — Good

The setter-level validation is correct and proportionate:

```java
public void setDefaultSrid(int defaultSrid) {
    Assert.isTrue(defaultSrid > 0, "oracle.database.spatial.default-srid must be greater than 0");
    this.defaultSrid = defaultSrid;
}

public void setDefaultDistanceUnit(String defaultDistanceUnit) {
    Assert.hasText(defaultDistanceUnit, "...");
    String trimmed = defaultDistanceUnit.trim();
    Assert.isTrue(!trimmed.contains("'"), "...");
    this.defaultDistanceUnit = trimmed;
}
```

The single-quote injection guard in the distance unit is important because the unit value flows into a string literal inside Oracle SQL (e.g., `'distance=100 unit=M'`). This is well-considered. The validation fires at application startup via Spring's property binding, which is exactly the right moment.

### `OracleSpatialGeoJsonConverter` — Excellent

The additions are correct:
- `fromGeoJsonSql(String, int)` overload for per-query SRID — correct signature.
- `distanceClause(Number, String)` overload for per-query unit — correct, with the same quote guard.
- `Assert.hasText` guards on both `bindExpression` and `geometryExpression`.

The Javadoc is accurate and useful. The doc comment on `fromGeoJsonSql(String, int)` correctly states "using the supplied SRID instead of the configured default," which is exactly the right framing for the overload pair.

### `OracleSpatialSqlBuilder` — Excellent

The Javadoc is the most significant improvement. The method comments accurately describe the Oracle SQL fragments produced, the bind parameter name convention (without leading colon), and the SDO_NN limitation:

> This method hardcodes Oracle operator number `1`, so callers using multiple `SDO_NN` operators in the same SQL statement should build that query manually.

The `normalize(mask)` behaviour is now also documented on `relatePredicate`. This is the right place for it rather than on the private method.

The per-SRID overloads for `filterPredicate`, `relatePredicate`, `geometryFromGeoJson`, and `insertGeometryExpression` are consistent with the converter overloads and complete the API surface.

### `OracleSpatialConditionalAutoConfigurationTest` — Good, One Minor Issue

```java
@Test
void backsOffWhenNoDataSourceBeanIsPresent() {
    contextRunner.run(context -> {
        assertThat(context).doesNotHaveBean(OracleSpatialGeoJsonConverter.class);
        assertThat(context).doesNotHaveBean(OracleSpatialSqlBuilder.class);
    });
}

@Test
void backsOffWhenOracleJdbcClassesAreMissing() {
    contextRunner
            .withUserConfiguration(TestDataSourceConfiguration.class)
            .withClassLoader(new FilteredClassLoader("oracle.jdbc.OracleConnection"))
            ...
}
```

Both tests are correct in what they verify. The `ApplicationContextRunner` pattern is the right approach for `@ConditionalOn*` tests — it does not start a full Spring Boot context and runs in milliseconds.

**Minor issue:** `new FilteredClassLoader("oracle.jdbc.OracleConnection")` uses the package-string constructor, which works here because `name.startsWith("oracle.jdbc.OracleConnection")` matches the class name. However, the idiomatic form for hiding a specific class is the class-reference constructor:

```java
new FilteredClassLoader(oracle.jdbc.OracleConnection.class)
```

Using the class reference is unambiguous, avoids the prefix-match subtlety, and matches the pattern used in Spring Boot's own auto-configuration tests. This is a low-priority cosmetic issue since the current form works correctly.

### `OracleSpatialIntegrationTest` — Good

The `blankRelateMaskDefaultsToAnyInteract` test is correctly implemented and proves the `normalize()` behaviour against a real database. The existing `spatialPredicatesWork` test now uses `SDO_NN` alone (without `SDO_WITHIN_DISTANCE`) which is the correct Oracle Spatial usage pattern.

**Observation:** The `@DynamicPropertySource` connects as `system` (Oracle superuser) rather than the `testuser` created by the container (`withUsername("testuser")`). This was present before and is technically fine for tests — `system` has the necessary privileges to create tables and insert into `USER_SDO_GEOM_METADATA`. However it is slightly inconsistent with the container configuration and may confuse readers. The sample test has the same pattern with its `USERNAME` env variable set to `"system"`.

### `LandmarkService.findNear` — Corrected

The P0 fix is correct:

```java
String distanceProjection = "SDO_GEOM.SDO_DISTANCE(geometry, "
        + sqlBuilder.geometryFromGeoJson("refGeometry")
        + ", 0.005, 'unit=" + geoJsonConverter.defaultDistanceUnit() + "') distance";
return jdbcClient.sql("select ..., " + distanceProjection
                + " from landmarks where "
                + sqlBuilder.withinDistancePredicate("geometry", "refGeometry", effectiveDistance)
                + " order by distance fetch first " + effectiveLimit + " rows only")
```

`SDO_WITHIN_DISTANCE` filters, `SDO_GEOM.SDO_DISTANCE` computes the actual metric distance for ordering, and `FETCH FIRST N ROWS ONLY` limits results. This is a standard and correct Oracle Spatial pattern for "find N nearest within a radius" queries.

The tolerance `0.005` in `SDO_GEOM.SDO_DISTANCE` is hardcoded. For WGS84 (SRID 4326) geodetic data, the tolerance parameter to `SDO_GEOM.SDO_DISTANCE` is specified in metres; `0.005` means 5 mm precision, which is reasonable. This could be worth a code comment since the magic number is not self-evident.

`findWithin` still uses `geometry` as both the column name and the bind parameter name, which was a P2 item from the previous review. See Section 3 below.

---

## Remaining Issues

### Issue 1 — Dead `OverrideBeans` Inner Class in `OracleSpatialAutoConfigurationTest`

`OracleSpatialAutoConfigurationTest` defines a `@TestConfiguration` inner class `OverrideBeans` with `srid=3857`, but the `TestApplication` in the same file only imports `Config.class`, not `OverrideBeans`:

```java
@SpringBootConfiguration
@EnableAutoConfiguration
@Import(Config.class)   // ← OverrideBeans is NOT imported
static class TestApplication {
}
```

Because `classes = TestApplication.class` is specified on `@SpringBootTest`, the `OverrideBeans` inner class is never registered in the application context. The test body correctly asserts the default `srid=4326`, which works only because the `OverrideBeans` are inactive.

The `OverrideBeans` class is an exact copy of what already appears in `OracleSpatialOverrideAutoConfigurationTest`, which is where it belongs. It should be removed from `OracleSpatialAutoConfigurationTest` to eliminate dead code and avoid confusion about what the test is actually testing.

### Issue 2 — README Missing Configuration Properties Table

The starter README (`oracle-spring-boot-starter-spatial/README.md`) now has the query guidance section and the VECTOR distinction. What is still missing is a configuration properties reference. Without it, users must read the `OracleSpatialProperties` source to discover the available knobs. This was a P1 recommendation in the previous review.

The following section should be added:

```markdown
## Configuration Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `oracle.database.spatial.enabled` | `boolean` | `true` | Enables or disables the spatial auto-configuration |
| `oracle.database.spatial.default-srid` | `int` | `4326` | SRID embedded in generated `SDO_UTIL.FROM_GEOJSON` calls; must be positive |
| `oracle.database.spatial.default-distance-unit` | `String` | `M` | Distance unit token appended to generated distance clauses; Oracle-supported values include `M`, `KM`, `UNIT=MILE` |
```

### Issue 3 — Seed SQL Does Not Specify SRID in `sdo_util.from_geojson` Calls

Both `spatial-init.sql` (data-tools test) and `init.sql` (sample test) insert seed rows without specifying the SRID:

```sql
insert into landmarks (..., geometry)
values (1, 'Ferry Building', 'MARKET',
    sdo_util.from_geojson('{"type":"Point","coordinates":[-122.3933,37.7955]}'));
```

Without the SRID argument, `SDO_UTIL.FROM_GEOJSON` returns a geometry with `null` SRID. The metadata row registers SRID 4326. The tests pass because Oracle is generally tolerant of null SRID when metadata is present, but this is inconsistent and is a misleading pattern for users who copy from the init scripts.

The consistent form — which matches how the starter generates SQL at runtime — is:

```sql
sdo_util.from_geojson('{"type":"Point","coordinates":[-122.3933,37.7955]}', null, 4326)
```

Both init scripts should be updated to use the three-argument form.

### Issue 4 — `findWithin` Column/Bind Name Collision (P2, Still Outstanding)

```java
jdbcClient.sql("... where "
        + sqlBuilder.filterPredicate("geometry", "geometry")        // col=geometry, bind=geometry
        + " and " + sqlBuilder.relatePredicate("geometry", "geometry", mask))
    .param("geometry", geometry)
```

Using `"geometry"` as both the table column name and the bind parameter name is syntactically correct — Spring's `JdbcClient` resolves bind parameters by the `:name` form in the SQL, which does not conflict with the unqualified column name. However it reads ambiguously and is a poor pattern to demonstrate in a sample that users will copy. Renaming the bind parameter to `refGeometry` (as was done in `findNear`) would be consistent and clearer.

### Issue 5 — SDO_NN Query Guidance Wording Is Imprecise

The README Query Guidance section states:

> Avoid combining `SDO_NN` and `SDO_WITHIN_DISTANCE` in the same simple `WHERE` clause unless you are intentionally building a more advanced Oracle Spatial query pattern.

The qualifier "simple... unless you are intentionally building a more advanced pattern" inadvertently suggests there are common advanced patterns where the combination is appropriate. Oracle's documentation advises against this combination without qualification. The guidance would be stronger without the escape clause:

> Do not combine `SDO_NN` and `SDO_WITHIN_DISTANCE` in the same `WHERE` clause. Use `SDO_WITHIN_DISTANCE` for radius filtering, `SDO_NN` for nearest-neighbor queries, or `SDO_WITHIN_DISTANCE` ordered by `SDO_GEOM.SDO_DISTANCE` when you need both a distance bound and a result count.

---

## Updated Priority Table

| Issue | Priority | Effort |
|-------|----------|--------|
| Dead `OverrideBeans` in `OracleSpatialAutoConfigurationTest` | P1 | Trivial — delete the inner class |
| README missing configuration properties table | P1 | Small — add ~10 lines |
| Seed SQL missing SRID in `from_geojson` calls | P1 | Trivial — update 3 rows in each of 2 SQL files |
| `findWithin` column/bind name collision | P2 | Trivial — rename bind param to `refGeometry` |
| SDO_NN README guidance wording | P2 | Trivial — one sentence edit |
| `FilteredClassLoader` string vs class reference | P3 | Trivial — cosmetic |
| `SDO_GEOM.SDO_DISTANCE` tolerance magic number | P3 | Add a code comment |

---

## Overall Assessment

The implementation is in very good shape and all significant concerns from the first review have been resolved. The P0 correctness bug is fixed correctly, the public API now has complete and useful Javadoc, the per-query SRID/unit overloads are implemented cleanly, and the new conditional tests cover the previously missing cases.

What remains are four small issues and one trivial one. None of them affect runtime correctness for the common path — the P1 seed SQL inconsistency is the most likely to confuse a real user copying the init script pattern. The dead `OverrideBeans` inner class and the missing README properties table are polish items that should be addressed before the PR is merged but require very little work.

**Recommendation: ready to merge after fixing the three P1 items.**
