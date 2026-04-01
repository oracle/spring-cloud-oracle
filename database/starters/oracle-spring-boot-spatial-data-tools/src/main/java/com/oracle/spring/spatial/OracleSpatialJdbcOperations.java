// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.spatial;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.util.Assert;

/**
 * Spring JDBC-oriented entry point for working with Oracle Spatial and
 * GeoJSON-backed {@code SDO_GEOMETRY} values.
 */
public class OracleSpatialJdbcOperations {
    private final OracleSpatialProperties properties;
    private final AtomicLong bindSequence = new AtomicLong();

    /**
     * Creates a new operations helper backed by the provided properties.
     *
     * @param properties spatial defaults used for SRID and distance units
     */
    public OracleSpatialJdbcOperations(OracleSpatialProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates a bindable geometry value using the configured default SRID.
     *
     * @param geoJson GeoJSON payload
     * @return spatial geometry wrapper
     */
    public SpatialGeometry geometry(String geoJson) {
        return geometry(geoJson, properties.getDefaultSrid());
    }

    /**
     * Creates a bindable geometry value using the supplied SRID.
     *
     * @param geoJson GeoJSON payload
     * @param srid Oracle Spatial SRID
     * @return spatial geometry wrapper
     */
    public SpatialGeometry geometry(String geoJson, int srid) {
        Assert.hasText(geoJson, "geoJson must not be blank");
        Assert.isTrue(srid > 0, "srid must be greater than 0");
        return new SpatialGeometry("spatialGeometry" + bindSequence.incrementAndGet(), geoJson, srid);
    }

    /**
     * Returns an expression that converts a bindable GeoJSON geometry into
     * {@code SDO_GEOMETRY}. This is useful for inserts, updates, and advanced
     * custom SQL.
     *
     * @param geometry geometry value to convert
     * @return bindable SQL expression
     */
    public SpatialExpression fromGeoJson(SpatialGeometry geometry) {
        Assert.notNull(geometry, "geometry must not be null");
        return expression("SDO_UTIL.FROM_GEOJSON(:" + geometry.bindName() + ", null, " + geometry.srid() + ")", geometry);
    }

    /**
     * Returns a GeoJSON projection expression for the given geometry column.
     *
     * @param geometryColumn geometry column or expression
     * @return SQL expression suitable for a select list
     */
    public SpatialExpression toGeoJson(String geometryColumn) {
        Assert.hasText(geometryColumn, "geometryColumn must not be blank");
        return expression("SDO_UTIL.TO_GEOJSON(" + geometryColumn + ")");
    }

    /**
     * Returns an {@code SDO_FILTER(...)} predicate.
     *
     * @param geometryColumn geometry column or expression
     * @param geometry bindable geometry value
     * @return bindable spatial predicate
     */
    public SpatialPredicate filter(String geometryColumn, SpatialGeometry geometry) {
        return predicate("SDO_FILTER(" + geometryColumn + ", " + fromGeoJsonSql(geometry) + ") = 'TRUE'", geometry);
    }

    /**
     * Returns an {@code SDO_RELATE(...)} predicate for the given relationship
     * mask.
     *
     * @param geometryColumn geometry column or expression
     * @param geometry bindable geometry value
     * @param mask relationship mask
     * @return bindable spatial predicate
     */
    public SpatialPredicate relate(String geometryColumn, SpatialGeometry geometry, SpatialRelationMask mask) {
        Assert.notNull(mask, "mask must not be null");
        return predicate("SDO_RELATE(" + geometryColumn + ", " + fromGeoJsonSql(geometry)
                + ", 'mask=" + mask.sqlValue() + "') = 'TRUE'", geometry);
    }

    /**
     * Returns an {@code SDO_WITHIN_DISTANCE(...)} predicate using the configured
     * default distance unit.
     *
     * @param geometryColumn geometry column or expression
     * @param geometry bindable geometry value
     * @param distance distance threshold
     * @return bindable spatial predicate
     */
    public SpatialPredicate withinDistance(String geometryColumn, SpatialGeometry geometry, Number distance) {
        return withinDistance(geometryColumn, geometry, distance, properties.getDefaultDistanceUnit());
    }

    /**
     * Returns an {@code SDO_WITHIN_DISTANCE(...)} predicate using the supplied
     * distance unit token.
     *
     * @param geometryColumn geometry column or expression
     * @param geometry bindable geometry value
     * @param distance distance threshold
     * @param unit Oracle distance unit token
     * @return bindable spatial predicate
     */
    public SpatialPredicate withinDistance(String geometryColumn, SpatialGeometry geometry, Number distance, String unit) {
        validateDistanceUnit(unit);
        Assert.notNull(distance, "distance must not be null");
        return predicate("SDO_WITHIN_DISTANCE(" + geometryColumn + ", " + fromGeoJsonSql(geometry)
                + ", '" + distanceClause(distance, unit) + "') = 'TRUE'", geometry);
    }

    /**
     * Returns an {@code SDO_NN(...)} predicate using Oracle operator id
     * {@code 1}.
     *
     * @param geometryColumn geometry column or expression
     * @param geometry bindable geometry value
     * @param numResults Oracle {@code sdo_num_res} value
     * @return bindable spatial predicate
     */
    public SpatialPredicate nearestNeighbor(String geometryColumn, SpatialGeometry geometry, int numResults) {
        Assert.isTrue(numResults > 0, "numResults must be greater than 0");
        return predicate("SDO_NN(" + geometryColumn + ", " + fromGeoJsonSql(geometry)
                + ", 'sdo_num_res=" + numResults + "', 1) = 'TRUE'", geometry);
    }

    /**
     * Returns the {@code SDO_NN_DISTANCE(1)} expression for use in select lists
     * or order clauses after {@link #nearestNeighbor(String, SpatialGeometry, int)}.
     *
     * @return SQL expression
     */
    public SpatialExpression nearestNeighborDistance() {
        return expression("SDO_NN_DISTANCE(1)");
    }

    /**
     * Returns a distance expression based on {@code SDO_GEOM.SDO_DISTANCE}
     * using the configured default unit and a caller-provided tolerance.
     *
     * @param geometryColumn geometry column or expression
     * @param geometry bindable geometry value
     * @param tolerance Oracle Spatial tolerance
     * @return bindable SQL expression
     */
    public SpatialExpression distance(String geometryColumn, SpatialGeometry geometry, Number tolerance) {
        return distance(geometryColumn, geometry, tolerance, properties.getDefaultDistanceUnit());
    }

    /**
     * Returns a distance expression based on {@code SDO_GEOM.SDO_DISTANCE}
     * using the supplied tolerance and unit.
     *
     * @param geometryColumn geometry column or expression
     * @param geometry bindable geometry value
     * @param tolerance Oracle Spatial tolerance
     * @param unit Oracle distance unit token
     * @return bindable SQL expression
     */
    public SpatialExpression distance(String geometryColumn, SpatialGeometry geometry, Number tolerance, String unit) {
        Assert.notNull(tolerance, "tolerance must not be null");
        validateDistanceUnit(unit);
        return expression("SDO_GEOM.SDO_DISTANCE(" + geometryColumn + ", "
                + fromGeoJsonSql(geometry) + ", " + tolerance + ", 'unit=" + unit.trim() + "')", geometry);
    }

    /**
     * Returns a Spring JDBC {@link RowMapper} that reads a GeoJSON projection
     * column from a result set.
     *
     * @param columnLabel result-set column label
     * @return row mapper for GeoJSON string results
     */
    public RowMapper<String> geoJsonRowMapper(String columnLabel) {
        return new OracleSpatialGeoJsonRowMapper(columnLabel);
    }

    /**
     * Applies bind parameters from the given spatial parts to a
     * {@link JdbcClient.StatementSpec}.
     *
     * @param statement JDBC statement spec
     * @param parts spatial parts carrying bind values
     * @return updated statement
     */
    public JdbcClient.StatementSpec bind(JdbcClient.StatementSpec statement, SpatialJdbcBindable... parts) {
        JdbcClient.StatementSpec current = statement;
        for (SpatialJdbcBindable part : parts) {
            if (part != null) {
                current = part.bind(current);
            }
        }
        return current;
    }

    private SpatialExpression expression(String expression) {
        return new SpatialExpression(expression, Map.of());
    }

    private SpatialExpression expression(String expression, SpatialGeometry geometry) {
        return new SpatialExpression(expression, parameters(geometry));
    }

    private SpatialPredicate predicate(String clause, SpatialGeometry geometry) {
        return new SpatialPredicate(clause, parameters(geometry));
    }

    private Map<String, Object> parameters(SpatialGeometry geometry) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put(geometry.bindName(), geometry.geoJson());
        return parameters;
    }

    private String fromGeoJsonSql(SpatialGeometry geometry) {
        Assert.notNull(geometry, "geometry must not be null");
        return "SDO_UTIL.FROM_GEOJSON(:" + geometry.bindName() + ", null, " + geometry.srid() + ")";
    }

    private String distanceClause(Number distance, String unit) {
        return "distance=" + distance + " unit=" + unit.trim();
    }

    private void validateDistanceUnit(String unit) {
        Assert.hasText(unit, "unit must not be blank");
        Assert.isTrue(!unit.trim().contains("'"), "unit must not contain single quotes");
    }
}
