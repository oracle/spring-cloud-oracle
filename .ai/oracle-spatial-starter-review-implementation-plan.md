# Oracle Spatial Starter Review Follow-Up Plan

## Purpose

This plan turns the recommendations in `oracle-spatial-starter-review.md` into a concrete implementation sequence. It is intentionally limited to planning only so we can review scope and tradeoffs before making code changes.

## Goals

- Address the architect's pre-merge concerns without expanding v1 beyond its intended scope.
- Preserve the current module structure and repository conventions.
- Prioritize correctness, API clarity, and test coverage over adding new features.

## Recommended Scope

### In scope for this follow-up

- Fix the sample application's `near` query so it uses a correct Oracle Spatial pattern.
- Improve the public spatial API with Javadoc and small overloads that reduce friction for real users.
- Add validation and documentation to `OracleSpatialProperties`.
- Add missing auto-configuration and behavior tests called out in the review.
- Clarify documentation so users understand:
  - this starter targets `SDO_GEOMETRY`, not Oracle AI `VECTOR`
  - the sample `GET /landmarks/near` endpoint expects compact GeoJSON in a query parameter
  - production applications should add input validation and use schema migrations for metadata/index setup

### Out of scope for this follow-up

- Introducing JPA / Hibernate Spatial support
- Introducing JTS or another geometry model
- Redesigning the sample API from `GET /landmarks/near` to a `POST` request
- Supporting every advanced Oracle Spatial edge case in v1
- Adding vector-search features related to Oracle `VECTOR`

## Workstreams

## 1. Correct the sample `near` query semantics

### Problem

The current sample combines `SDO_WITHIN_DISTANCE` and `SDO_NN` in the same query path. The review flags this as an incorrect Oracle Spatial usage pattern that may yield errors or misleading behavior.

### Plan

- Refactor `LandmarkService.findNear(...)` to use one primary spatial strategy instead of combining operators in the same predicate set.
- Keep the endpoint contract compatible with the existing sample unless we decide otherwise during review.
- Prefer a query shape that is easy to explain in the README and stable for sample/demo purposes.

### Recommended implementation direction

- Treat `distance` as the primary search constraint for the sample endpoint.
- Use `SDO_WITHIN_DISTANCE` to filter candidates.
- Order the filtered results by `SDO_NN_DISTANCE(...)` expression or another valid Oracle-supported distance ordering pattern already compatible with the builder.
- Apply `FETCH FIRST N ROWS ONLY` using the requested `limit`.

### Follow-up note

- Document in code and README that `SDO_NN` and `SDO_WITHIN_DISTANCE` should not be casually combined in a single WHERE clause.

## 2. Improve `OracleSpatialGeoJsonConverter`

### Problem

The current converter forces a single SRID and distance unit per bean instance, which makes per-query overrides awkward.

### Plan

- Keep the current methods for backward compatibility.
- Add overloads for the common per-query override cases:
  - `fromGeoJsonSql(String bindExpression, int srid)`
  - `distanceClause(Number distance, String unit)`
- Add Javadoc describing expected inputs:
  - bind expressions should already be valid SQL fragments
  - callers pass bind parameter references such as `:geometry`
  - null or invalid values are caller errors

### Validation approach

- Do not over-engineer this with a large type system in v1.
- Prefer clear Javadoc plus lightweight validation where it materially improves failure messages.

## 3. Improve `OracleSpatialSqlBuilder`

### Problem

The builder is functional but under-documented, and a few methods are unclear to users without reading generated SQL.

### Plan

- Add Javadoc to every public method.
- Document for each method:
  - the SQL fragment it generates
  - what kind of argument each parameter expects
  - whether the return value is intended for `SELECT`, `WHERE`, or `ORDER BY`
  - any Oracle-specific caveats
- Explicitly document:
  - blank `mask` values normalize to `ANYINTERACT`
  - the distinction between `nearestNeighborDistanceExpression()` and `nearestNeighborDistanceProjection(alias)`
  - the `SDO_NN_DISTANCE(1)` identifier assumption and its limitations

### Optional small API improvements

- Evaluate whether any builder methods should accept a bind name that is more explicit in sample code, such as `refGeometry`, without renaming the existing public API.
- Keep compatibility with existing tests and sample code unless a rename materially improves clarity.

## 4. Strengthen `OracleSpatialProperties`

### Problem

The properties are sensible but undocumented and lightly validated.

### Plan

- Add class-level and field-level Javadoc so Spring configuration metadata is useful in IDEs.
- Add validation for:
  - `defaultSrid` must be positive
  - `defaultDistanceUnit` must be non-blank and in a supported format

### Validation options

- Preferred: lightweight setter or initialization validation with clear exception messages.
- Alternative: Bean Validation annotations if that fits the repository's current style in starter modules.

### Decision to make before implementation

- Decide whether to validate `defaultDistanceUnit` as:
  - a small allowlist of common units such as `M`, `KM`, `MILE`
  - a looser string-format check that preserves Oracle flexibility

### Recommendation

- Use a looser validation rule for v1 so we do not reject Oracle-supported formats like `UNIT=MILE`.

## 5. Expand auto-configuration tests

### Problem

Two important conditional cases are currently untested.

### Plan

- Add a test verifying that no spatial beans are created when no `DataSource` bean is available.
- Add a test verifying that no spatial beans are created when Oracle JDBC classes are absent, using `FilteredClassLoader`.
- Keep the existing tests for:
  - default bean creation
  - `enabled=false`
  - user bean override

### Test style

- Follow Spring Boot auto-configuration test conventions.
- Prefer focused context-runner style tests if the existing module already supports that cleanly; otherwise stay consistent with the current test style in this module.

## 6. Expand integration and behavior tests

### Problem

The review identified a few behavioral cases that should be covered explicitly.

### Plan

- Add a test for blank `SDO_RELATE` mask normalization to `ANYINTERACT`.
- Add or update tests around the sample `near` behavior after the query fix.
- Ensure the integration tests reflect the intended contract:
  - nearest-neighbor support works
  - within-distance support works
  - the sample does not rely on an invalid combination of both patterns

### Test design guidance

- Keep the passing Oracle Testcontainers setup already established for the spatial modules.
- Reuse the current `gvenzl/oracle-free:23.26.0-full-faststart` image for spatial tests unless we consciously revisit image strategy later.

## 7. Improve README and user-facing docs

### Problem

The library and sample docs do not yet make a few important boundaries explicit.

### Plan

- Update the spatial starter README and site docs to clarify:
  - this module is for geographic/topographic `SDO_GEOMETRY`
  - it is not a vector-search starter for Oracle AI embeddings
  - schema metadata and spatial indexes belong in migrations or setup SQL
  - sample `GET /landmarks/near` expects compact GeoJSON in the query string
  - production applications should add validation around request payloads and query parameters

### Nice-to-have

- Include a short section with query composition guidance:
  - when to use `SDO_FILTER`
  - when to use `SDO_RELATE`
  - when to use `SDO_WITHIN_DISTANCE`
  - when to use `SDO_NN`

## Proposed Sequence

1. Fix the sample `near` query semantics first, because it is the most important correctness issue.
2. Add or update tests that lock in the corrected query behavior.
3. Add missing auto-configuration conditional tests.
4. Add `OracleSpatialProperties` validation and Javadoc.
5. Add converter and SQL builder overloads plus public API Javadoc.
6. Refresh README and site docs last so they reflect the final API and sample behavior.

## Expected File Areas

- `database/starters/oracle-spring-boot-spatial-data-tools/src/main/java/com/oracle/spring/spatial/`
- `database/starters/oracle-spring-boot-spatial-data-tools/src/test/java/com/oracle/spring/spatial/`
- `database/starters/oracle-spring-boot-starter-samples/oracle-spring-boot-sample-spatial/src/main/java/com/oracle/database/spring/spatial/`
- `database/starters/oracle-spring-boot-starter-samples/oracle-spring-boot-sample-spatial/src/test/java/com/oracle/database/spring/spatial/`
- `database/starters/oracle-spring-boot-starter-spatial/README.md`
- `site/docs/database/spatial.md`

## Risks and Tradeoffs

- Adding validation that is too strict could reject valid Oracle-specific distance-unit strings.
- Changing sample query behavior without carefully updating tests could make the REST sample less intuitive.
- Expanding the public API should remain incremental; we should avoid turning v1 into a full query DSL.

## Open Questions For Review

- Should the sample `near` endpoint remain a `GET` for plan consistency, or should we defer any API-shape improvement to a later iteration?
- Do we want only documentation for `SDO_NN_DISTANCE(1)` limitations, or do we want a configurable operator number in v1?
- How strict should property validation be for `defaultDistanceUnit`?
- Do we want to keep all current public method names exactly as-is and only add overloads/Javadoc, or is there appetite for small naming cleanup where confusion is high?

## Agreed Direction

- Keep `GET /landmarks/near` for v1 to stay aligned with the original plan and avoid unnecessary API churn. Document clearly that the `geometry` query parameter should be compact GeoJSON.
- Validate `oracle.database.spatial.default-distance-unit` loosely rather than with a strict enum so Oracle-supported values such as `UNIT=MILE` remain valid.
- Treat the hardcoded `SDO_NN_DISTANCE(1)` operator identifier as a documented v1 limitation rather than introducing configurable operator numbering in this pass.
- Preserve the current public method names and improve the API by adding overloads and Javadoc instead of renaming methods.
- Keep this follow-up as a focused hardening pass: correctness fixes, test coverage, documentation, and a few low-risk API improvements, but no broader DSL or transport redesign.

## Suggested Outcome

If we agree with this plan, the implementation can proceed as a focused hardening pass rather than a redesign. The likely deliverable is a merge-ready v1 with:

- corrected sample query behavior
- stronger docs and configuration metadata
- a more practical SQL helper API
- fuller conditional and integration test coverage
