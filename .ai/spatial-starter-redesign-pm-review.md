# PM Review: Redesigned Oracle Spring Boot Spatial Starter

*Review date: 2026-04-01*
*Based on: oracle-spatial-jdbc-idiomatic-redesign-plan.md (approved)*

---

## Overall Assessment

The redesign successfully addresses the reviewer feedback. The API is now meaningfully Spring-native rather than a raw string utility. The core question — *does this serve spatial customers well and showcase the right capabilities?* — is answered more convincingly than before.

---

## What the Redesign Gets Right

**The reviewer's concern is resolved.** The API is no longer a string concatenation utility. Developers inject one bean, work with typed objects (`SpatialGeometry`, `SpatialPredicate`, `SpatialExpression`), and the `bind()` method integrates directly with `JdbcClient.StatementSpec`. This is recognisably Spring.

**`SpatialRelationMask` enum is a genuine improvement.** It directly fixes the risk flagged in the v1 PM review — a developer can no longer pass `"INTERSECTS"` and get a silent runtime error from Oracle. The enum is self-documenting and covers the full set of valid masks including `COVERS`.

**`SDO_GEOM.SDO_DISTANCE` is now a first-class operation.** The `distance()` method on `OracleSpatialJdbcOperations` closes one of the top gaps from the v1 review. Customers building "find nearest N with distance in result" queries — the most common spatial use case — have what they need.

**The sample is meaningfully better.** `LandmarkService` reads like idiomatic Spring code. The `findNear` method in particular is clean: geometry, distance expression, and predicate are all named variables before the SQL is assembled. A developer reading this will understand the pattern immediately.

**`geoJsonRowMapper` is a proper Spring JDBC integration hook.** Aligns with the JSON data-tools pattern called out in the redesign plan.

**Documentation has kept pace.** The "What You Inject vs What You Build" section in `spatial.md` is the right mental model explanation. The four query pattern examples are concrete and directly usable.

---

## Remaining Concerns

### 1. The `bind()` pattern is still somewhat leaky (v2 candidate)

The developer still calls `.expression()` or `.clause()` inline inside the SQL string, and passes the same objects to `bind()` separately. Example from `findNear`:

```java
spatial.bind(
    jdbcClient.sql("select ... " + distanceExpression.selection("distance")
        + " from landmarks where " + withinDistance.clause() + ...),
    distanceExpression, withinDistance)
```

There is a subtle risk: a developer creates a `SpatialPredicate` but forgets to include it in the `bind()` varargs, and their bind parameter goes unset. The types do not prevent this. Acceptable for v1, but worth tracking as a v2 usability gap.

### 2. `resolveMask()` in the sample throws an unhandled exception — fix before ship

`SpatialRelationMask.valueOf(mask.trim().toUpperCase())` in `LandmarkService` will throw `IllegalArgumentException` if the API caller passes an unsupported string like `"INTERSECTS"`. This surfaces as a 500 in the sample app rather than a 400. The sample is the canonical usage example, so this should be fixed before release. (Sample issue, not a starter issue.)

### 3. `SDO_GEOM.SDO_BUFFER` still absent (carry forward from v1 review)

Buffer generation was flagged as a high-priority gap in the v1 PM review. A customer who wants "find all properties within 500m of a railway line" needs a buffer expression. Not in scope for this redesign — track for v2.

### 4. Bind-name sequencer worth a documentation note (minor)

`OracleSpatialJdbcOperations` uses an `AtomicLong` to generate unique bind names (`spatialGeometry1`, `spatialGeometry2`, etc.). Developers who inspect generated SQL in logs may be confused by incrementing parameter names across requests. Functionally fine, but a one-line note in the usage docs would head off confusion.

---

## Summary

| Area | v1 | Redesign |
|---|---|---|
| Spring-native API | No | Yes |
| `SDO_RELATE` mask safety | Risk (raw string) | Fixed (enum) |
| `SDO_GEOM.SDO_DISTANCE` | Missing | Added |
| `RowMapper` support | Missing | Added |
| Sample readability | Acceptable | Good |
| Documentation quality | Good | Very good |
| `bind()` completeness risk | N/A | Minor — developer can omit parts |
| `SDO_GEOM.SDO_BUFFER` | Missing | Still missing |
| Sample error handling | N/A | Needs attention (500 on bad mask) |

**Recommendation:** Approve with the `resolveMask` error handling fixed in the sample. Remaining concerns are either v2 items or minor documentation additions.
