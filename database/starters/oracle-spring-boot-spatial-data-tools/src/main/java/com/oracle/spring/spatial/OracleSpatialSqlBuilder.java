// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.spatial;

import java.util.Locale;

/**
 * Generates SQL fragments for common Oracle Spatial operations while keeping the
 * application API GeoJSON-first.
 */
public class OracleSpatialSqlBuilder {
    private final OracleSpatialGeoJsonConverter geoJsonConverter;

    public OracleSpatialSqlBuilder(OracleSpatialGeoJsonConverter geoJsonConverter) {
        this.geoJsonConverter = geoJsonConverter;
    }

    public String geometryFromGeoJson(String bindName) {
        return geoJsonConverter.fromGeoJsonSql(":" + bindName);
    }

    public String geometryToGeoJson(String geometryExpression) {
        return geoJsonConverter.toGeoJsonSql(geometryExpression);
    }

    public String insertGeometryExpression(String geoJsonBindName) {
        return geometryFromGeoJson(geoJsonBindName);
    }

    public String filterPredicate(String geometryColumn, String geoJsonBindName) {
        return "SDO_FILTER(" + geometryColumn + ", " + geometryFromGeoJson(geoJsonBindName) + ") = 'TRUE'";
    }

    public String relatePredicate(String geometryColumn, String geoJsonBindName, String mask) {
        return "SDO_RELATE(" + geometryColumn + ", " + geometryFromGeoJson(geoJsonBindName)
                + ", 'mask=" + normalize(mask) + "') = 'TRUE'";
    }

    public String withinDistancePredicate(String geometryColumn, String geoJsonBindName, Number distance) {
        return "SDO_WITHIN_DISTANCE(" + geometryColumn + ", " + geometryFromGeoJson(geoJsonBindName)
                + ", '" + geoJsonConverter.distanceClause(distance) + "') = 'TRUE'";
    }

    public String nearestNeighborPredicate(String geometryColumn, String geoJsonBindName, int numResults) {
        return "SDO_NN(" + geometryColumn + ", " + geometryFromGeoJson(geoJsonBindName)
                + ", 'sdo_num_res=" + numResults + "', 1) = 'TRUE'";
    }

    public String nearestNeighborDistanceProjection(String alias) {
        return "SDO_NN_DISTANCE(1) AS " + alias;
    }

    public String nearestNeighborDistanceExpression() {
        return "SDO_NN_DISTANCE(1)";
    }

    private String normalize(String value) {
        return value == null ? "ANYINTERACT" : value.trim().toUpperCase(Locale.ROOT);
    }
}
