// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.spatial;

/**
 * Helper for producing SQL fragments and bind values for GeoJSON / SDO_GEOMETRY conversion.
 */
public class OracleSpatialGeoJsonConverter {
    private final OracleSpatialProperties properties;

    public OracleSpatialGeoJsonConverter(OracleSpatialProperties properties) {
        this.properties = properties;
    }

    public String fromGeoJsonSql(String bindExpression) {
        return "SDO_UTIL.FROM_GEOJSON(" + bindExpression + ")";
    }

    public String toGeoJsonSql(String geometryExpression) {
        return "SDO_UTIL.TO_GEOJSON(" + geometryExpression + ")";
    }

    public int defaultSrid() {
        return properties.getDefaultSrid();
    }

    public String defaultDistanceUnit() {
        return properties.getDefaultDistanceUnit();
    }

    public String distanceClause(Number distance) {
        return "distance=" + distance + " unit=" + defaultDistanceUnit();
    }
}
