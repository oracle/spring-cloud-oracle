// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.spatial;

import java.util.List;

import com.oracle.spring.spatial.OracleSpatialSqlBuilder;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class LandmarkService {
    private final JdbcClient jdbcClient;
    private final OracleSpatialSqlBuilder sqlBuilder;

    public LandmarkService(JdbcClient jdbcClient, OracleSpatialSqlBuilder sqlBuilder) {
        this.jdbcClient = jdbcClient;
        this.sqlBuilder = sqlBuilder;
    }

    public Landmark create(Landmark landmark) {
        jdbcClient.sql("insert into landmarks (id, name, category, geometry) values (:id, :name, :category, "
                        + sqlBuilder.insertGeometryExpression("geometry") + ")")
                .param("id", landmark.id())
                .param("name", landmark.name())
                .param("category", landmark.category())
                .param("geometry", landmark.geometry())
                .update();
        return getById(landmark.id());
    }

    public Landmark getById(Long id) {
        return jdbcClient.sql("select id, name, category, "
                        + sqlBuilder.geometryToGeoJson("geometry") + " as geometry from landmarks where id = :id")
                .param("id", id)
                .query((rs, rowNum) -> new Landmark(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("geometry")
                ))
                .single();
    }

    public List<Landmark> findNear(String geometry, Integer distance, Integer limit) {
        int effectiveDistance = distance == null ? 2000 : distance;
        int effectiveLimit = limit == null ? 3 : limit;
        return jdbcClient.sql("select id, name, category, "
                        + sqlBuilder.geometryToGeoJson("geometry") + " as geometry, "
                        + sqlBuilder.nearestNeighborDistance("geometry", "geometry", "distance")
                        + " from landmarks where "
                        + sqlBuilder.withinDistancePredicate("geometry", "geometry", effectiveDistance)
                        + " and " + sqlBuilder.nearestNeighborPredicate("geometry", "geometry", effectiveLimit)
                        + " order by distance fetch first " + effectiveLimit + " rows only")
                .param("geometry", geometry)
                .query((rs, rowNum) -> new Landmark(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("geometry")
                ))
                .list();
    }

    public List<Landmark> findWithin(String geometry, String mask) {
        return jdbcClient.sql("select id, name, category, "
                        + sqlBuilder.geometryToGeoJson("geometry") + " as geometry from landmarks where "
                        + sqlBuilder.filterPredicate("geometry", "geometry")
                        + " and " + sqlBuilder.relatePredicate("geometry", "geometry", mask))
                .param("geometry", geometry)
                .query((rs, rowNum) -> new Landmark(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("geometry")
                ))
                .list();
    }
}
