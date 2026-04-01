# Oracle Spring Boot Spatial Starter — PM Review & V2 Planning

*Review date: 2026-04-01*
*Branch reviewed: spatial-starter*

This document records the product management review of the v1 spatial starter as a basis for v2 planning.

---

## Overall Assessment

Solid, well-scoped v1 that covers the essential read/write operations. The GeoJSON-first design is the right call for modern Spring developers. The concerns below are about gaps that will frustrate real spatial customers, not about what's been done wrong.

---

## What's Working Well

**Scope is appropriate.** The four core Oracle Spatial operators — `SDO_FILTER`, `SDO_RELATE`, `SDO_WITHIN_DISTANCE`, `SDO_NN` — are the ones most Spring developers will actually reach for. Covering them all in a first release is the right call.

**GeoJSON-first is the right default.** It aligns with how REST APIs, mapping libraries (Leaflet, Mapbox, OpenLayers), and spatial web tooling works. Developers won't have to convert anything at the application layer.

**The two-tier API makes sense.** `OracleSpatialGeoJsonConverter` as a lower-level building block and `OracleSpatialSqlBuilder` as the higher-level helper gives developers an escape hatch without abandoning the starter.

**The sample application is concrete and useful.** San Francisco landmarks is tangible, the REST API structure matches what developers actually build, and the `near` vs `within` distinction is an important pattern to demonstrate.

**Documentation links to Oracle docs.** The "Further Reading" section is well-curated and the usage notes on anti-patterns (no `SDO_NN` + `SDO_WITHIN_DISTANCE` in the same `WHERE`) are the kind of guidance that saves developers hours.

---

## V2 Candidates: Gaps That Will Frustrate Spatial Customers

### 1. SDO_GEOM Analytical Functions — Highest Priority

`SDO_GEOM` functions are half the reason customers use Oracle Spatial for analysis, not just lookup. There is currently no support for:

- `SDO_GEOM.SDO_DISTANCE(a, b, tolerance)` — distance between two stored geometries
- `SDO_GEOM.SDO_AREA(geom, tolerance)` — polygon area calculation
- `SDO_GEOM.SDO_BUFFER(geom, distance, tolerance)` — buffer zone generation
- `SDO_GEOM.SDO_INTERSECTION`, `SDO_UNION`, `SDO_DIFFERENCE` — geometry construction

A customer building "find all properties within the flood buffer zone" has no help from the starter today. They must hand-write all `SDO_GEOM` SQL themselves. Minimum v2 scope: `SDO_DISTANCE` and `SDO_BUFFER` as documented helpers on `OracleSpatialSqlBuilder`.

### 2. SDO_RELATE Mask Constants or Enum — Significant Usability Risk

The `relatePredicate()` method takes a raw `String mask` parameter. Oracle's valid mask values are not obvious:

```
ANYINTERACT, CONTAINS, COVEREDBY, DISJOINT, EQUAL, INSIDE, ON,
OVERLAPBDYDISJOINT, OVERLAPBDYINTERSECT, TOUCH
```

A developer who passes `"INTERSECTS"` (the PostGIS equivalent) will get a runtime database error with no helpful message. A `SpatialRelationMask` enum or a documented constants class would prevent a category of support tickets and make the API self-documenting. This is a low-effort, high-impact change.

### 3. Sample Application — Geometry Type Diversity

The sample inserts only Point geometries. Golden Gate Park is stored as a Point, not the polygon it actually is. This misses the chance to show:

- Storing a Polygon and querying *which points fall inside it* using `SDO_RELATE` with `INSIDE`
- Storing a LineString (e.g., a route or path) and querying proximity to it

Developers building anything beyond "store a pin on a map" need to see that Oracle Spatial handles complex geometry types and that the starter works with all of them.

### 4. Schema Setup Guidance — New User Friction

The starter correctly says "manage your own schema," but the DDL for `USER_SDO_GEOM_METADATA` registration and `MDSYS.SPATIAL_INDEX_V2` creation is unfamiliar to most Spring developers. It is currently buried in the sample SQL scripts. A "Getting Started" section in `spatial.md` showing the three-step schema setup (table → metadata → index) would reduce first-run friction substantially.

### 5. WKT/WKB Support

Oracle supports `SDO_UTIL.FROM_WKTGEOMETRY`, `TO_WKTGEOMETRY`, `FROM_WKBGEOMETRY`, `TO_WKBGEOMETRY`. Many spatial tools (QGIS, PostGIS migrations, GIS file exports) produce WKT or WKB. A customer migrating data from PostGIS or reading shapefiles will hit this gap immediately. At minimum v2 should either add WKT/WKB helper methods or explicitly document the limitation with a workaround pattern.

---

## V2 Candidates: Oracle 26ai Positioning

This starter ships alongside Oracle 26ai but does not position itself relative to the Oracle AI story. Oracle's differentiator is combining spatial with AI — e.g., using vector search to find semantically similar documents *near a location*. The docs do not mention this angle at all.

Even a single paragraph in `spatial.md` — "Oracle Spatial works alongside Oracle AI Vector Search; combine spatial predicates with vector similarity in the same query for location-aware AI applications" — would make the 26ai positioning clear and hint at the integration pattern without requiring the starter to do more. A companion sample or recipe showing a combined spatial + vector query would be a strong differentiator.

---

## V2 Candidates: Minor Documentation Issues

- `spatial.md` lists all `OracleSpatialSqlBuilder` methods but provides no inline SQL example output. A developer reading the docs cannot tell what SQL `nearestNeighborDistanceProjection("dist")` actually generates without running it. Add one-line output examples for each method.
- The README `Example` section is too minimal — it shows injection but no query. The site doc's `create()` example is much better and should be the README example too.
- `default-distance-unit` documentation lists `M`, `KM`, and `UNIT=MILE` as valid values but does not explain why the format differs between `M`/`KM` and `UNIT=MILE`. That inconsistency will confuse developers who try to compose unit strings themselves.

---

## Priority Summary

| Gap | Priority | Effort |
|---|---|---|
| SDO_GEOM analytical functions (distance, area, buffer) | High | Medium |
| SDO_RELATE mask constants / enum | High | Low |
| Sample geometry diversity (Polygon, LineString) | Medium | Low |
| Schema setup Getting Started section in docs | Medium | Low |
| WKT/WKB support or documented limitation | Medium | Low–Medium |
| Oracle 26ai / AI Vector positioning in docs | Medium | Low |
| Inline SQL output examples in method docs | Low | Low |
| README example with a real query | Low | Low |
| Distance unit format explanation | Low | Low |
