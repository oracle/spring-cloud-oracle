# Oracle Spatial Starter — PM Review Follow-Up Plan

## Purpose

This plan captures the actionable follow-up work from the PM review in `.ai/spatial-starter-redesign-pm-review.md`.

This is a planning document only. No implementation should begin until we review and agree on the scope.

## Overall Assessment

I agree with the PM review's main conclusion:

- the redesign has solved the core architectural concern
- the new API is meaningfully more Spring-native
- only a small amount of follow-up work looks necessary before ship

I do **not** think the remaining `bind()` ergonomics concern should block this PR. That is a valid usability observation, but it is better treated as a v2 design improvement than a late redesign within the current PR.

Likewise, I do **not** think `SDO_GEOM.SDO_BUFFER` should be pulled into this PR. It is a legitimate gap, but it is feature expansion rather than cleanup of the approved redesign.

## Recommended Scope

## Ship Now

### 1. Fix invalid `mask` handling in the sample app

The current sample implementation converts the incoming request string to:

```java
SpatialRelationMask.valueOf(mask.trim().toUpperCase(Locale.ROOT))
```

If the caller passes an unsupported value such as `INTERSECTS`, the sample will currently fail with an `IllegalArgumentException`, which surfaces as an HTTP 500.

This is not a starter defect, but it is a sample defect, and the sample is a primary usage example for the new API.

#### Proposed change

- keep the request contract unchanged: `WithinLandmarkRequest` should continue to accept `String mask`
- validate and translate the string inside the sample application
- return a client error (`400 Bad Request`) instead of a server error for invalid masks
- provide a short, clear error message listing the supported values or at least naming the invalid mask

#### Candidate implementation options

Option A: catch and translate inside `LandmarkService`

- update `resolveMask()` to throw a sample-specific exception with a helpful message
- add a controller-level exception handler or a global `@RestControllerAdvice`

Option B: validate in the controller

- parse the request mask before calling the service
- return `400` from the web layer directly

#### Recommendation

Use **Option A** with a small sample-specific exception plus `@RestControllerAdvice`.

Why:

- keeps the controller simple
- keeps mask parsing logic in one place
- produces a cleaner example of how application code can adapt starter enums to public HTTP contracts

### 2. Add a documentation note about generated bind names

`OracleSpatialJdbcOperations` generates bind names like:

- `spatialGeometry1`
- `spatialGeometry2`
- `spatialGeometry3`

This is functionally correct, but developers inspecting SQL logs may be surprised that the names increment across requests and are not tied to their domain field names.

#### Proposed change

Add a brief note to the spatial documentation explaining that:

- bind names are generated internally by the starter
- they are intentionally opaque
- they may increment over the lifetime of the bean
- callers should not depend on specific bind parameter names

#### Documentation targets

- `site/docs/database/spatial.md`
- optionally `database/starters/oracle-spring-boot-starter-spatial/README.md` if we want the shorter README to mention it too

#### Recommendation

At minimum, add the note to `site/docs/database/spatial.md`.

## Track For V2

### 3. Improve the `bind()` ergonomics

The PM review is right that this usage pattern is still a bit leaky:

```java
spatial.bind(statement, distanceExpression, withinDistance)
```

A caller can forget to pass one of the parts to `bind()` even though they already used its SQL fragment in the statement string.

#### Why I would not change this now

- the current design is still a major improvement over the raw string-builder API
- a good fix likely needs a more opinionated rendering/binding model
- changing this now risks destabilising the approved redesign

#### V2 exploration candidates

- query-part rendering objects that contribute SQL and bindings together in one step
- helper APIs that build a whole `WHERE` or `SELECT` clause from typed parts
- a tighter `JdbcClient` integration that reduces the chance of forgetting bind contributors

### 4. Add `SDO_GEOM.SDO_BUFFER`

This remains a good enhancement target for a future iteration.

#### Why I would not add it now

- it is feature expansion, not review cleanup
- it would widen the API surface late in the PR
- it deserves its own sample and docs once added

#### V2 scope candidate

- add a buffer expression method on `OracleSpatialJdbcOperations`
- document how to combine buffer generation with `filter`, `relate`, or distance-based searches
- add at least one focused sample/test case

## Proposed Implementation Sequence

If we decide to proceed with the ship-now items, I would implement them in this order:

1. Add sample-level invalid-mask exception handling and map it to HTTP 400.
2. Add or update a sample test covering an invalid `mask` request.
3. Add the bind-name explanation to the docs.
4. Do a final doc pass to ensure the sample behavior described in docs still matches the implementation.

## Expected Files To Touch

Likely code/docs files:

- `database/starters/oracle-spring-boot-starter-samples/oracle-spring-boot-sample-spatial/src/main/java/com/oracle/database/spring/spatial/LandmarkService.java`
- `database/starters/oracle-spring-boot-starter-samples/oracle-spring-boot-sample-spatial/src/main/java/com/oracle/database/spring/spatial/LandmarkController.java` or a new advice/exception file in the same package
- `database/starters/oracle-spring-boot-starter-samples/oracle-spring-boot-sample-spatial/src/test/java/com/oracle/database/spring/spatial/SpatialSampleApplicationTest.java`
- `site/docs/database/spatial.md`
- optionally `database/starters/oracle-spring-boot-starter-spatial/README.md`

## Recommendation

Proceed with only the two pre-ship cleanup items:

- sample invalid-mask handling
- bind-name documentation note

Defer:

- `bind()` ergonomics redesign
- `SDO_GEOM.SDO_BUFFER`

That keeps this PR disciplined while still addressing the one genuine release-quality issue identified in the PM review.
