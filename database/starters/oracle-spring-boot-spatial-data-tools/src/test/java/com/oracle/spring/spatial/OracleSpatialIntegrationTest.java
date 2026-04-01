// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.spatial;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(classes = OracleSpatialIntegrationTest.TestApplication.class)
@Sql(scripts = "/spatial-init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class OracleSpatialIntegrationTest {
    @Container
    @ServiceConnection
    static OracleContainer oracleContainer = new OracleContainer("gvenzl/oracle-free:23.26.1-full-faststart")
            .withStartupTimeout(Duration.ofMinutes(2))
            .withUsername("testuser")
            .withPassword("testpwd");

    @Autowired
    OracleSpatialJdbcOperations spatial;

    @Autowired
    JdbcClient jdbcClient;

    @Test
    void geoJsonRoundTrip() {
        SpatialExpression geometry = spatial.toGeoJson("geometry");
        String geoJson = jdbcClient.sql("select " + geometry.selection("geometry") + " from landmarks where id = :id")
                .param("id", 1L)
                .query(spatial.geoJsonRowMapper("geometry"))
                .single();

        assertThat(geoJson).contains("\"Point\"");
        assertThat(geoJson).contains("-122.3933");
    }

    @Test
    void spatialPredicatesWork() {
        String point = "{\"type\":\"Point\",\"coordinates\":[-122.4194,37.7749]}";
        String polygon = "{\"type\":\"Polygon\",\"coordinates\":[[[-122.53,37.70],[-122.35,37.70],[-122.35,37.83],[-122.53,37.83],[-122.53,37.70]]]}";
        SpatialGeometry pointGeometry = spatial.geometry(point);
        SpatialGeometry polygonGeometry = spatial.geometry(polygon);
        SpatialPredicate filter = spatial.filter("geometry", polygonGeometry);
        SpatialPredicate relate = spatial.relate("geometry", polygonGeometry, SpatialRelationMask.ANYINTERACT);
        SpatialPredicate withinDistance = spatial.withinDistance("geometry", pointGeometry, 2000);
        SpatialPredicate nearestNeighbor = spatial.nearestNeighbor("geometry", pointGeometry, 1);

        Long filterCount = spatial.bind(
                        jdbcClient.sql("select count(*) from landmarks where " + filter.clause()),
                        filter)
                .query(Long.class)
                .single();
        assertThat(filterCount).isGreaterThanOrEqualTo(2L);

        Long relateCount = spatial.bind(
                        jdbcClient.sql("select count(*) from landmarks where " + relate.clause()),
                        relate)
                .query(Long.class)
                .single();
        assertThat(relateCount).isGreaterThanOrEqualTo(2L);

        Long withinDistanceCount = spatial.bind(
                        jdbcClient.sql("select count(*) from landmarks where " + withinDistance.clause()),
                        withinDistance)
                .query(Long.class)
                .single();
        assertThat(withinDistanceCount).isGreaterThanOrEqualTo(1L);

        String nearestName = spatial.bind(
                        jdbcClient.sql("select name from landmarks where "
                                + nearestNeighbor.clause()
                                + " order by " + spatial.nearestNeighborDistance().expression()),
                        nearestNeighbor)
                .query(String.class)
                .single();
        assertThat(nearestName).isEqualTo("Union Square");
    }

    @Test
    void distanceExpressionWorks() {
        String point = "{\"type\":\"Point\",\"coordinates\":[-122.4194,37.7749]}";
        SpatialGeometry pointGeometry = spatial.geometry(point);
        SpatialExpression distance = spatial.distance("geometry", pointGeometry, 0.005);

        Double nearestDistance = spatial.bind(
                        jdbcClient.sql("select " + distance.selection("distance")
                                + " from landmarks where id = :id"),
                        distance)
                .param("id", 2L)
                .query(Double.class)
                .single();

        assertThat(nearestDistance).isGreaterThanOrEqualTo(0.0d);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
    }
}
