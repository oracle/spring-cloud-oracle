// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.spatial;

import java.util.Locale;

import org.springframework.util.Assert;

/**
 * Generates SQL fragments for common Oracle Spatial operations while keeping the
 * application API GeoJSON-first.
 */
public class OracleSpatialSqlBuilder {
    private final OracleSpatialGeoJsonConverter geoJsonConverter;

    public OracleSpatialSqlBuilder(OracleSpatialGeoJsonConverter geoJsonConverter) {
        this.geoJsonConverter = geoJsonConverter;
    }

    /**
     * Returns an {@code SDO_UTIL.FROM_GEOJSON(...)} expression for a named bind
     * parameter using the configured default SRID.
     *
     * @param bindName bind parameter name without the leading colon
     * @return SQL fragment suitable for inserts and predicates
     */
    public String geometryFromGeoJson(String bindName) {
        Assert.hasText(bindName, "bindName must not be blank");
        return geoJsonConverter.fromGeoJsonSql(":" + bindName);
    }

    /**
     * Returns an {@code SDO_UTIL.FROM_GEOJSON(...)} expression for a named bind
     * parameter using the supplied SRID.
     *
     * @param bindName bind parameter name without the leading colon
     * @param srid Oracle Spatial SRID to embed in the generated SQL
     * @return SQL fragment suitable for inserts and predicates
     */
    public String geometryFromGeoJson(String bindName, int srid) {
        Assert.hasText(bindName, "bindName must not be blank");
        return geoJsonConverter.fromGeoJsonSql(":" + bindName, srid);
    }

    /**
     * Returns an {@code SDO_UTIL.TO_GEOJSON(...)} expression for a geometry
     * column or SQL expression.
     *
     * @param geometryExpression geometry column or SQL expression
     * @return SQL projection fragment
     */
    public String geometryToGeoJson(String geometryExpression) {
        return geoJsonConverter.toGeoJsonSql(geometryExpression);
    }

    /**
     * Returns an insert/update expression that converts a named GeoJSON bind
     * parameter into {@code SDO_GEOMETRY} using the configured default SRID.
     *
     * @param geoJsonBindName bind parameter name without the leading colon
     * @return SQL expression suitable for insert or update statements
     */
    public String insertGeometryExpression(String geoJsonBindName) {
        return geometryFromGeoJson(geoJsonBindName);
    }

    /**
     * Returns an insert/update expression that converts a named GeoJSON bind
     * parameter into {@code SDO_GEOMETRY} using the supplied SRID.
     *
     * @param geoJsonBindName bind parameter name without the leading colon
     * @param srid Oracle Spatial SRID to embed in the generated SQL
     * @return SQL expression suitable for insert or update statements
     */
    public String insertGeometryExpression(String geoJsonBindName, int srid) {
        return geometryFromGeoJson(geoJsonBindName, srid);
    }

    /**
     * Returns an {@code SDO_FILTER(...)} predicate that compares a geometry
     * column against a GeoJSON bind parameter using the configured default SRID.
     *
     * @param geometryColumn geometry column or SQL expression
     * @param geoJsonBindName bind parameter name without the leading colon
     * @return SQL predicate fragment for a {@code WHERE} clause
     */
    public String filterPredicate(String geometryColumn, String geoJsonBindName) {
        return "SDO_FILTER(" + geometryColumn + ", " + geometryFromGeoJson(geoJsonBindName) + ") = 'TRUE'";
    }

    /**
     * Returns an {@code SDO_FILTER(...)} predicate that compares a geometry
     * column against a GeoJSON bind parameter using the supplied SRID.
     *
     * @param geometryColumn geometry column or SQL expression
     * @param geoJsonBindName bind parameter name without the leading colon
     * @param srid Oracle Spatial SRID to embed in the generated SQL
     * @return SQL predicate fragment for a {@code WHERE} clause
     */
    public String filterPredicate(String geometryColumn, String geoJsonBindName, int srid) {
        return "SDO_FILTER(" + geometryColumn + ", " + geometryFromGeoJson(geoJsonBindName, srid) + ") = 'TRUE'";
    }

    /**
     * Returns an {@code SDO_RELATE(...)} predicate that compares a geometry
     * column against a GeoJSON bind parameter using the configured default SRID.
     * Blank masks normalize to {@code ANYINTERACT}.
     *
     * @param geometryColumn geometry column or SQL expression
     * @param geoJsonBindName bind parameter name without the leading colon
     * @param mask Oracle Spatial mask such as {@code ANYINTERACT} or
     *             {@code INSIDE}; blank values normalize to {@code ANYINTERACT}
     * @return SQL predicate fragment for a {@code WHERE} clause
     */
    public String relatePredicate(String geometryColumn, String geoJsonBindName, String mask) {
        return "SDO_RELATE(" + geometryColumn + ", " + geometryFromGeoJson(geoJsonBindName)
                + ", 'mask=" + normalize(mask) + "') = 'TRUE'";
    }

    /**
     * Returns an {@code SDO_RELATE(...)} predicate that compares a geometry
     * column against a GeoJSON bind parameter using the supplied SRID.
     * Blank masks normalize to {@code ANYINTERACT}.
     *
     * @param geometryColumn geometry column or SQL expression
     * @param geoJsonBindName bind parameter name without the leading colon
     * @param mask Oracle Spatial mask such as {@code ANYINTERACT} or
     *             {@code INSIDE}; blank values normalize to {@code ANYINTERACT}
     * @param srid Oracle Spatial SRID to embed in the generated SQL
     * @return SQL predicate fragment for a {@code WHERE} clause
     */
    public String relatePredicate(String geometryColumn, String geoJsonBindName, String mask, int srid) {
        return "SDO_RELATE(" + geometryColumn + ", " + geometryFromGeoJson(geoJsonBindName, srid)
                + ", 'mask=" + normalize(mask) + "') = 'TRUE'";
    }

    /**
     * Returns an {@code SDO_WITHIN_DISTANCE(...)} predicate using the configured
     * default SRID and distance unit.
     *
     * @param geometryColumn geometry column or SQL expression
     * @param geoJsonBindName bind parameter name without the leading colon
     * @param distance numeric distance threshold
     * @return SQL predicate fragment for a {@code WHERE} clause
     */
    public String withinDistancePredicate(String geometryColumn, String geoJsonBindName, Number distance) {
        return "SDO_WITHIN_DISTANCE(" + geometryColumn + ", " + geometryFromGeoJson(geoJsonBindName)
                + ", '" + geoJsonConverter.distanceClause(distance) + "') = 'TRUE'";
    }

    /**
     * Returns an {@code SDO_WITHIN_DISTANCE(...)} predicate using the configured
     * default SRID and the supplied distance unit token.
     *
     * @param geometryColumn geometry column or SQL expression
     * @param geoJsonBindName bind parameter name without the leading colon
     * @param distance numeric distance threshold
     * @param unit Oracle unit token such as {@code M}, {@code KM}, or
     *             {@code UNIT=MILE}
     * @return SQL predicate fragment for a {@code WHERE} clause
     */
    public String withinDistancePredicate(String geometryColumn, String geoJsonBindName, Number distance, String unit) {
        return "SDO_WITHIN_DISTANCE(" + geometryColumn + ", " + geometryFromGeoJson(geoJsonBindName)
                + ", '" + geoJsonConverter.distanceClause(distance, unit) + "') = 'TRUE'";
    }

    /**
     * Returns an {@code SDO_NN(...)} predicate using the configured default
     * SRID. This method hardcodes Oracle operator number {@code 1}, so callers
     * using multiple {@code SDO_NN} operators in the same SQL statement should
     * build that query manually.
     *
     * @param geometryColumn geometry column or SQL expression
     * @param geoJsonBindName bind parameter name without the leading colon
     * @param numResults Oracle {@code sdo_num_res} value
     * @return SQL predicate fragment for a {@code WHERE} clause
     */
    public String nearestNeighborPredicate(String geometryColumn, String geoJsonBindName, int numResults) {
        return "SDO_NN(" + geometryColumn + ", " + geometryFromGeoJson(geoJsonBindName)
                + ", 'sdo_num_res=" + numResults + "', 1) = 'TRUE'";
    }

    /**
     * Returns an {@code SDO_NN_DISTANCE(1)} projection with an alias. Use this
     * in a {@code SELECT} list after applying {@link #nearestNeighborPredicate}.
     *
     * @param alias projection alias to append after the expression
     * @return SQL projection fragment
     */
    public String nearestNeighborDistanceProjection(String alias) {
        Assert.hasText(alias, "alias must not be blank");
        return "SDO_NN_DISTANCE(1) " + alias;
    }

    /**
     * Returns the raw {@code SDO_NN_DISTANCE(1)} expression. Use this in an
     * {@code ORDER BY} clause after applying {@link #nearestNeighborPredicate}.
     *
     * @return SQL expression fragment
     */
    public String nearestNeighborDistanceExpression() {
        return "SDO_NN_DISTANCE(1)";
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? "ANYINTERACT" : value.trim().toUpperCase(Locale.ROOT);
    }
}
