// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.spatial;

import org.springframework.util.Assert;

/**
 * Helper for producing Oracle SQL fragments that convert between GeoJSON and
 * {@code SDO_GEOMETRY}.
 */
public class OracleSpatialGeoJsonConverter {
    private final OracleSpatialProperties properties;

    /**
     * Creates a new converter backed by the provided spatial properties.
     *
     * @param properties spatial defaults used for SRID and distance units
     */
    public OracleSpatialGeoJsonConverter(OracleSpatialProperties properties) {
        this.properties = properties;
    }

    /**
     * Returns an {@code SDO_UTIL.FROM_GEOJSON(...)} SQL fragment using the
     * configured default SRID.
     *
     * @param bindExpression SQL bind or expression to convert, for example
     *                       {@code :geometry}
     * @return Oracle SQL fragment suitable for inserts or predicate arguments
     */
    public String fromGeoJsonSql(String bindExpression) {
        return fromGeoJsonSql(bindExpression, defaultSrid());
    }

    /**
     * Returns an {@code SDO_UTIL.FROM_GEOJSON(...)} SQL fragment using the
     * supplied SRID instead of the configured default.
     *
     * @param bindExpression SQL bind or expression to convert, for example
     *                       {@code :geometry}
     * @param srid Oracle Spatial SRID to embed in the generated SQL
     * @return Oracle SQL fragment suitable for inserts or predicate arguments
     */
    public String fromGeoJsonSql(String bindExpression, int srid) {
        Assert.hasText(bindExpression, "bindExpression must not be blank");
        Assert.isTrue(srid > 0, "srid must be greater than 0");
        return "SDO_UTIL.FROM_GEOJSON(" + bindExpression + ", null, " + srid + ")";
    }

    /**
     * Returns an {@code SDO_UTIL.TO_GEOJSON(...)} SQL fragment.
     *
     * @param geometryExpression geometry column or SQL expression to convert
     * @return Oracle SQL fragment suitable for a {@code SELECT} projection
     */
    public String toGeoJsonSql(String geometryExpression) {
        Assert.hasText(geometryExpression, "geometryExpression must not be blank");
        return "SDO_UTIL.TO_GEOJSON(" + geometryExpression + ")";
    }

    /**
     * Returns the configured default SRID.
     *
     * @return default SRID used for GeoJSON conversion
     */
    public int defaultSrid() {
        return properties.getDefaultSrid();
    }

    /**
     * Returns the configured default distance unit token.
     *
     * @return default distance unit appended to generated distance clauses
     */
    public String defaultDistanceUnit() {
        return properties.getDefaultDistanceUnit();
    }

    /**
     * Returns an Oracle distance clause using the configured default unit.
     *
     * @param distance numeric distance value
     * @return clause formatted for {@code SDO_WITHIN_DISTANCE}
     */
    public String distanceClause(Number distance) {
        return distanceClause(distance, defaultDistanceUnit());
    }

    /**
     * Returns an Oracle distance clause using the supplied unit token.
     *
     * @param distance numeric distance value
     * @param unit Oracle unit token such as {@code M}, {@code KM}, or
     *             {@code UNIT=MILE}
     * @return clause formatted for {@code SDO_WITHIN_DISTANCE}
     */
    public String distanceClause(Number distance, String unit) {
        Assert.notNull(distance, "distance must not be null");
        Assert.hasText(unit, "unit must not be blank");
        String trimmedUnit = unit.trim();
        Assert.isTrue(!trimmedUnit.contains("'"), "unit must not contain single quotes");
        return "distance=" + distance + " unit=" + trimmedUnit;
    }
}
