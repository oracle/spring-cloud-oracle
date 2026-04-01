# PR 255 Review Response Plan

## Source

This plan responds to the current review comments on PR `oracle/spring-cloud-oracle#255`.

## Review Comments To Address

1. "we probably should not use string sql builders. can something similar be done with jdbcclient?"
2. "did you mean to check these files in?" on `.ai/oracle-spatial-starter-plan.md`
3. "prefer @ServiceConnection"
4. "use 23.26.1"

## Goals

- Resolve the straightforward review items directly in this branch.
- Separate the larger API design concern from the simple cleanup items so we do not mix a potentially broad redesign into a near-merge branch without agreement.
- Keep the PR focused and easy to review.

## Recommended Approach

## 1. Remove accidental `.ai` planning/review files from the PR

### Problem

The PR currently includes local planning and review artifacts under `.ai/`, and the reviewer has explicitly questioned whether they were meant to be checked in.

### Plan

- Remove the `.ai` files from the PR branch:
  - `.ai/SPATIAL_STARTER_V2_REVIEW.md`
  - `.ai/oracle-spatial-starter-plan.md`
  - `.ai/oracle-spatial-starter-review-implementation-plan.md`
  - `.ai/oracle-spatial-starter-review-revision-2-plan.md`
  - `.ai/oracle-spatial-starter-review.md`
- Double-check whether any similar review artifact under the starter module path should also be removed if it is not intended for the repository.
- Re-run `git diff --stat` after removal to confirm the PR scope contains only product code, tests, and docs meant for merge.

### Expected Outcome

- The PR becomes cleaner and avoids repository-noise concerns.

## 2. Upgrade Oracle Free Testcontainers image version to `23.26.1`

### Problem

The reviewer asked to use `23.26.1` instead of `23.26.0`.

### Plan

- Update all spatial Testcontainers references from:
  - `gvenzl/oracle-free:23.26.0-full-faststart`
  to:
  - `gvenzl/oracle-free:23.26.1-full-faststart`
- Apply this consistently in:
  - `OracleSpatialIntegrationTest`
  - `SpatialSampleApplicationTest`
- Search the spatial modules for any remaining `23.26.0` references after the change.

### Verification

- Run the same Docker-backed tests already used for spatial validation:
  - `mvn -pl oracle-spring-boot-spatial-data-tools clean test`
  - `mvn -pl oracle-spring-boot-starter-samples/oracle-spring-boot-sample-spatial -am clean test`

## 3. Prefer `@ServiceConnection` for Testcontainers wiring

### Problem

The sample test currently uses `@DynamicPropertySource`, and the reviewer prefers `@ServiceConnection`.

### Current State

- `SpatialSampleApplicationTest` uses `@DynamicPropertySource`.
- `OracleSpatialIntegrationTest` also uses manual property registration.
- The sample application currently reads datasource settings from custom placeholders:
  - `JDBC_URL`
  - `USERNAME`
  - `PASSWORD`
- That means adopting `@ServiceConnection` cleanly will likely require normalizing datasource configuration toward standard Spring Boot `spring.datasource.*` property usage, at least in tests or possibly in the sample app config.

### Plan

- Verify the repository’s current Spring Boot / Testcontainers version supports `@ServiceConnection` with `OracleContainer`.
- Refactor the spatial sample test first:
  - annotate the container with `@ServiceConnection`
  - remove `@DynamicPropertySource`
  - update the sample datasource configuration to use standard Spring Boot datasource binding if needed
- Evaluate whether the data-tools integration test can also move to `@ServiceConnection`, or whether it should stay as-is because it is not a full application sample.

### Recommendation

- Prefer updating the sample test for sure, because that is the clearest fit for `@ServiceConnection`.
- Treat the integration test as optional for the same refactor if it remains clean and low-risk.

### Verification

- Re-run the sample and data-tools tests after the refactor to confirm Testcontainers startup and datasource binding still work.

## 4. Address the SQL builder design comment deliberately

### Problem

The reviewer questioned whether the starter should use string SQL builders at all, and suggested something more `JdbcClient`-native.

### Why This Needs Care

- The current starter API is intentionally built around Oracle SQL fragment generation for `JdbcClient` / `JdbcTemplate`.
- Replacing raw SQL fragments with a more structured API could become a larger design change than the other PR comments.
- This area likely needs explicit agreement before implementation because it changes the public API shape and the starter’s core design.

### Plan

- First, evaluate feasible alternatives that still fit Spring JDBC idioms:
  - wrapper helpers around `JdbcClient.StatementSpec`
  - predicate/projection helper objects instead of raw strings
  - a small composable SQL fragment type owned by the starter
- Compare each option against the current API on:
  - simplicity
  - readability in user code
  - compatibility with plain `JdbcClient`
  - migration cost for this PR
- Decide whether:
  - to redesign in this PR, or
  - to keep the current string-fragment API and open a follow-up design issue for a v2 API

### Recommendation

- Do not immediately redesign the starter in the same pass as the other three review comments.
- Prepare a short decision note after the evaluation:
  - if a low-risk `JdbcClient`-native improvement exists, implement it
  - otherwise respond in the PR that the current API is intentionally SQL-fragment based and propose a follow-up issue for a more structured v2 design

### Deliverable

- A short written recommendation before code changes in this area, so we do not destabilize a nearly-merge-ready branch without clear agreement.

## Proposed Execution Order

1. Remove `.ai` files from the PR.
2. Upgrade the container image to `23.26.1`.
3. Refactor the sample test to use `@ServiceConnection`, and optionally the integration test if it stays clean.
4. Re-run the spatial tests.
5. Separately evaluate the SQL-builder design comment and decide whether it belongs in this PR or a follow-up issue.

## Expected File Areas

- `.ai/`
- `database/starters/oracle-spring-boot-spatial-data-tools/src/test/java/com/oracle/spring/spatial/OracleSpatialIntegrationTest.java`
- `database/starters/oracle-spring-boot-starter-samples/oracle-spring-boot-sample-spatial/src/test/java/com/oracle/database/spring/spatial/SpatialSampleApplicationTest.java`
- `database/starters/oracle-spring-boot-starter-samples/oracle-spring-boot-sample-spatial/src/main/resources/application.yaml`
- Possibly other sample/test config files if `@ServiceConnection` requires standard datasource property wiring

## Success Criteria

- The PR no longer includes stray planning files.
- Spatial tests use `23.26.1`.
- At least the sample test uses `@ServiceConnection` cleanly.
- We have an explicit, review-ready response for the SQL-builder design comment rather than ignoring it.
