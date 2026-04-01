// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.spatial;

import java.util.Arrays;
import java.util.Locale;
import java.util.List;

import com.oracle.spring.spatial.OracleSpatialJdbcOperations;
import com.oracle.spring.spatial.SpatialExpression;
import com.oracle.spring.spatial.SpatialGeometry;
import com.oracle.spring.spatial.SpatialPredicate;
import com.oracle.spring.spatial.SpatialRelationMask;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class LandmarkService {
    private final JdbcClient jdbcClient;
    private final OracleSpatialJdbcOperations spatial;

    public LandmarkService(JdbcClient jdbcClient,
                           OracleSpatialJdbcOperations spatial) {
        this.jdbcClient = jdbcClient;
        this.spatial = spatial;
    }

    public Landmark create(Landmark landmark) {
        SpatialGeometry geometry = spatial.geometry(landmark.geometry());
        SpatialExpression insertGeometry = spatial.fromGeoJson(geometry);
        spatial.bind(
                        jdbcClient.sql("insert into landmarks (id, name, category, geometry) values (:id, :name, :category, "
                                + insertGeometry.expression() + ")"),
                        insertGeometry)
                .param("id", landmark.id())
                .param("name", landmark.name())
                .param("category", landmark.category())
                .update();
        return getById(landmark.id());
    }

    public Landmark getById(Long id) {
        SpatialExpression geometry = spatial.toGeoJson("geometry");
        return jdbcClient.sql("select id, name, category, "
                        + geometry.selection("geometry") + " from landmarks where id = :id")
                .param("id", id)
                .query(this::mapLandmark)
                .single();
    }

    public List<Landmark> findNear(String geometry, Integer distance, Integer limit) {
        int effectiveDistance = distance == null ? 2000 : distance;
        int effectiveLimit = limit == null ? 3 : limit;
        SpatialGeometry referenceGeometry = spatial.geometry(geometry);
        SpatialExpression projectedGeometry = spatial.toGeoJson("geometry");
        SpatialExpression distanceExpression = spatial.distance("geometry", referenceGeometry, 0.005);
        SpatialPredicate withinDistance = spatial.withinDistance("geometry", referenceGeometry, effectiveDistance);
        return spatial.bind(
                        jdbcClient.sql("select id, name, category, "
                                + projectedGeometry.selection("geometry") + ", "
                                + distanceExpression.selection("distance")
                                + " from landmarks where "
                                + withinDistance.clause()
                        + " order by distance fetch first " + effectiveLimit + " rows only")
                        , distanceExpression, withinDistance)
                .query(this::mapLandmark)
                .list();
    }

    public List<Landmark> findWithin(String geometry, String mask) {
        SpatialGeometry referenceGeometry = spatial.geometry(geometry);
        SpatialExpression projectedGeometry = spatial.toGeoJson("geometry");
        SpatialPredicate filter = spatial.filter("geometry", referenceGeometry);
        SpatialPredicate relate = spatial.relate("geometry", referenceGeometry, resolveMask(mask));
        return spatial.bind(
                        jdbcClient.sql("select id, name, category, "
                                + projectedGeometry.selection("geometry") + " from landmarks where "
                                + filter.clause()
                                + " and " + relate.clause()),
                        filter, relate)
                .query(this::mapLandmark)
                .list();
    }

    private Landmark mapLandmark(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new Landmark(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getString("geometry")
        );
    }

    private SpatialRelationMask resolveMask(String mask) {
        if (mask == null || mask.isBlank()) {
            return SpatialRelationMask.ANYINTERACT;
        }
        try {
            return SpatialRelationMask.valueOf(mask.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InvalidSpatialMaskException(mask, supportedMasks());
        }
    }

    private String supportedMasks() {
        return Arrays.stream(SpatialRelationMask.values())
                .map(Enum::name)
                .toList()
                .toString();
    }
}
