# Oracle Spatial Starter — Spring JDBC Idiomatic Redesign Plan

## Purpose

This plan updates the earlier redesign proposal based on reviewer feedback and on patterns already used in this repository. The goal is to replace the current public API based on raw SQL string fragments with something that feels more like a Spring starter: dependency aggregation plus Spring JDBC integration points.

This is a planning document only. No implementation should begin until we review and agree on the target design.

## Important Change in Assumptions

The current spatial API has **not** been released. That means:

- we do **not** need to preserve compatibility with the current string-builder API
- we do **not** need a staged migration plan
- we can redesign the public surface cleanly now if we agree on a better shape

This is a major simplification and should push us toward choosing the best API, not the most backward-compatible one.

## Reviewer Direction

The key reviewer guidance is:

- the starter should provide dependency aggregation and Spring API integrations
- if we are writing a SQL utility, it should work with `JdbcClient`, `JdbcTemplate`, `RowMapper`, or other Spring abstractions
- raw SQL strings as the main public API are problematic

## Repo Patterns Worth Reusing

## 1. JSON data-tools uses Spring integration objects, not raw SQL fragments

The clearest relevant pattern is `JSONBRowMapper<T>` in `oracle-spring-boot-json-data-tools`:

- it exposes a Spring `RowMapper<T>`
- it integrates Oracle-specific behavior with Spring JDBC
- application code consumes a Spring abstraction rather than ad hoc strings

This suggests the spatial starter should expose Spring-oriented integration types where possible.

## 2. Duality module keeps SQL building internal

`DualityViewBuilder` in `oracle-spring-boot-json-relational-duality-views` does build SQL, but:

- it owns the build process internally
- callers do not splice together partial SQL fragments
- the builder produces and executes coherent statements as part of a Spring-managed component

This suggests a better spatial design would:

- keep SQL generation internal to the starter
- expose Spring-friendly operations and mappers
- avoid making application code concatenate Oracle-specific fragments manually

## Design Goal

Redesign the spatial starter so that:

- the public API is Spring JDBC-oriented
- Oracle SQL generation remains internal implementation detail as much as possible
- application code works with Spring-friendly objects or operations rather than raw SQL snippets
- the starter remains lightweight and Oracle-specific where needed

## Non-Goals

- Building a JPA / Hibernate Spatial starter
- Building a large query DSL comparable to jOOQ
- Introducing Spring Data repository support in the first redesign pass
- Hiding Oracle Spatial concepts completely

## Current API Problems

### 1. The main public API returns raw SQL strings

`OracleSpatialSqlBuilder` returns fragments such as:

- `SDO_FILTER(...) = 'TRUE'`
- `SDO_RELATE(...) = 'TRUE'`
- `SDO_WITHIN_DISTANCE(...) = 'TRUE'`
- `SDO_NN(...) = 'TRUE'`

That forces application code to do string concatenation around Oracle-specific SQL.

### 2. The current API is Spring-managed but not Spring-native

The builder and converter are injectable beans, but their public return types are plain strings. This does not meaningfully integrate with `JdbcClient`, `JdbcTemplate`, or `RowMapper`.

### 3. Clause intent is implicit

Some methods are meant for:

- `SELECT`
- `WHERE`
- `ORDER BY`

but the API does not encode those distinctions, so misuse is easy.

### 4. Bind handling is too low-level

Callers must understand how `"shape"` turns into `:shape` and how to keep bind names aligned across SQL fragments and `JdbcClient` calls.

## Recommended Direction

## Core idea

Replace the public string-builder API with a **Spring JDBC integration layer** built around:

- Spring `RowMapper` support for spatial projections
- Spring-oriented spatial query operations
- internal SQL generation that callers do not assemble manually

## Top-level recommendation

Introduce a new primary bean:

- `OracleSpatialJdbcOperations`

This bean should become the main spatial entry point for Spring JDBC users.

It should be designed for use with:

- `JdbcClient`
- `JdbcTemplate`

and should coordinate:

- geometry conversion
- bind handling
- predicate assembly
- projection/mapping helpers

## Proposed Public API Shape

## 1. `OracleSpatialJdbcOperations`

This bean would expose higher-level methods that produce Spring-friendly query parts or bind helpers rather than raw strings.

Candidate responsibilities:

- create bindable geometry parameters from GeoJSON
- provide projection helpers for `SDO_GEOMETRY -> GeoJSON`
- provide predicate/operator abstractions for filter, relate, within-distance, nearest-neighbor
- provide optional ordering/projection helpers for distance-based queries
- provide convenience mapping helpers where useful

### Candidate method families

- geometry parameter creation
  - e.g. `geoJson(String json)`
  - e.g. `geoJson(String json, int srid)`
- projection helpers
  - e.g. `geoJsonColumn(String geometryColumn)`
- predicate helpers
  - e.g. `filter(String geometryColumn, SpatialGeometry value)`
  - e.g. `relate(String geometryColumn, SpatialGeometry value, SpatialRelationMask mask)`
  - e.g. `withinDistance(String geometryColumn, SpatialGeometry value, Number distance)`
  - e.g. `nearestNeighbor(String geometryColumn, SpatialGeometry value, int numResults)`
- ordering helpers
  - e.g. `distanceOrder(String geometryColumn, SpatialGeometry value)`

These methods should return typed objects, not strings.

## 2. Typed query-part objects

Introduce small immutable types such as:

- `SpatialGeometry`
- `SpatialPredicate`
- `SpatialProjection`
- `SpatialOrder`

These are not intended to become a giant DSL. Their purpose is to:

- encode intent
- carry Oracle-specific SQL rendering internally
- make the public API more explicit and less stringly typed

### Important distinction from the old API

The application should not call `.sql()` on these objects and manually concatenate them in ordinary usage unless absolutely necessary.

Instead, `OracleSpatialJdbcOperations` should provide helper methods that integrate them into Spring JDBC usage.

## 3. Spring `RowMapper` support

Add row-mapper support inspired by `JSONBRowMapper<T>`.

Candidate additions:

- `GeoJsonRowMapper`
  - maps a selected GeoJSON column into `String`
- `SpatialRowMappers`
  - helper factory for common row-mapping patterns
- optional record/domain mapper helpers for sample-like cases

### Why this matters

This is the clearest place where the starter can provide a truly Spring-native abstraction instead of a raw SQL utility.

## 4. Explicit mask support instead of raw string masks

The PM review already called out the risk around raw `SDO_RELATE` masks.

The redesign should replace raw string masks with something like:

- `SpatialRelationMask` enum

This aligns with the same overall goal:

- fewer unstructured strings
- more self-documenting Spring application code

## Candidate Design Options

## Option A — Spring JDBC integration bean plus typed query parts (Recommended)

### Characteristics

- `OracleSpatialJdbcOperations` is the main API
- typed spatial query-part objects exist internally/publicly
- row mappers are added
- SQL rendering remains internal

### Why this fits repo patterns

- like JSON data-tools, it exposes Spring JDBC-friendly integration points
- like duality views, it keeps SQL-building ownership inside the starter

### Recommendation

This should be the primary design target.

## Option B — Keep typed query parts but skip RowMapper work initially

### Characteristics

- redesign the query API
- defer row mappers

### Why it is weaker

- misses the most obvious Spring integration hook already proven useful in JSON data-tools
- responds only partially to the reviewer feedback

### Recommendation

Not preferred. We should include at least basic `RowMapper` support in the redesign.

## Option C — Build repository abstractions now

### Characteristics

- Spring Data JDBC repository fragments or custom repository support

### Why it is premature

- larger scope
- depends on getting the core JDBC integration API right first
- not necessary to answer the reviewer’s current concern

### Recommendation

Defer repository support until after the new JDBC-oriented API exists.

## Reframed Role of Existing Classes

## `OracleSpatialSqlBuilder`

Recommendation:

- do **not** keep this as the main public API
- either remove it entirely or reduce it to an internal implementation detail

Because the current API is unreleased, we should feel free to replace it rather than carrying it forward.

## `OracleSpatialGeoJsonConverter`

Recommendation:

- likely keep the core conversion logic
- but reconsider whether it remains a prominently documented public bean

Two reasonable options:

1. keep it public as a lower-level advanced helper
2. fold its responsibilities into `OracleSpatialJdbcOperations` and make it internal/package-private

### Current recommendation

Keep it for now as an internal building block until we settle the final top-level API. We can decide later whether it still deserves public status.

## What “Idiomatic” Should Mean Here

For this starter, “idiomatic” should mean:

- users inject Spring beans that help them use Oracle Spatial with `JdbcClient` / `JdbcTemplate`
- users do not manually assemble Oracle SQL fragments in normal usage
- mapping support is provided through Spring interfaces like `RowMapper`
- Oracle-specific SQL still exists, but it is encapsulated behind starter-owned components

It does **not** need to mean:

- no SQL knowledge required
- full repository abstraction on day one
- a fully generic persistence model independent of Oracle Spatial

## Sample Application Direction

The spatial sample should be rewritten to demonstrate the new design.

Instead of:

```java
jdbcClient.sql("select ... " + sqlBuilder.geometryToGeoJson("geometry") + " ...")
```

the sample should show:

- injecting `OracleSpatialJdbcOperations`
- using spatial predicates/projections owned by the starter
- using Spring-friendly row mapping where appropriate

The sample should become the canonical usage example for the new API.

## Testing Plan

## 1. Replace string-builder API tests with operations-oriented tests

Add tests for:

- geometry parameter creation
- relation mask enum usage
- projection helpers
- distance helpers
- row mappers

## 2. Keep real Oracle integration coverage

Retain Testcontainers integration tests against Oracle Free to verify:

- generated SQL is still correct
- binds are applied correctly
- mapping still works end to end

## 3. Update sample test to reflect the new idiomatic API

The sample should validate:

- app wiring
- endpoint behavior
- underlying starter usage pattern

## Documentation Plan

- Rewrite starter docs around `OracleSpatialJdbcOperations`
- Add examples showing:
  - injection into Spring services
  - row mapper usage
  - relation mask enum usage
  - distance and near-query patterns
- Remove emphasis on raw SQL fragments from README and site docs

## Implementation Sequence

1. Define the target public API for `OracleSpatialJdbcOperations`.
2. Define the supporting types:
   - `SpatialGeometry`
   - `SpatialPredicate`
   - `SpatialProjection`
   - `SpatialOrder`
   - `SpatialRelationMask`
3. Decide the fate of `OracleSpatialGeoJsonConverter`:
   - internal only, or
   - advanced public helper
4. Implement basic row-mapper support.
5. Rebuild the sample app on the new API.
6. Update tests.
7. Rewrite docs.

## Open Questions For Discussion

1. Should `OracleSpatialJdbcOperations` itself expose “apply to JdbcClient” helpers, or should it only produce typed spatial components?
2. How much Spring-specific behavior should live in the query-part objects themselves?
3. Should `SpatialRelationMask` be an enum only, or enum plus escape hatch for advanced masks?
4. Should the first redesign pass include any repository-oriented abstraction at all, or explicitly defer it?
5. Should `OracleSpatialGeoJsonConverter` survive as a public bean, or should we collapse conversion behind the new operations API?

## Recommended Decision

For the next discussion, I recommend we align on this target:

- primary API: `OracleSpatialJdbcOperations`
- include Spring `RowMapper` support from the start
- keep SQL building internal to the starter
- remove `OracleSpatialSqlBuilder` as the intended public API
- defer repository support until after the JDBC-oriented API is stable

If we agree on that direction, the redesign can be implemented cleanly without worrying about backward compatibility with the current unreleased string-builder API.
