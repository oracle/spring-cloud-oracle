// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.spatial;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = OracleSpatialIntegrationTest.TestApplication.class)
@Testcontainers
@Sql("/spatial-init.sql")
public class OracleSpatialIntegrationTest {
    @Container
    static OracleContainer oracleContainer = new OracleContainer("gvenzl/oracle-free:23.26.0-slim-faststart")
            .withStartupTimeout(Duration.ofMinutes(2))
            .withUsername("testuser")
            .withPassword("testpwd");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", oracleContainer::getJdbcUrl);
        registry.add("spring.datasource.username", oracleContainer::getUsername);
        registry.add("spring.datasource.password", oracleContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "oracle.jdbc.OracleDriver");
        registry.add("spring.datasource.type", () -> "oracle.ucp.jdbc.PoolDataSourceImpl");
    }

    @Autowired
    OracleSpatialGeoJsonConverter converter;

    @Autowired
    OracleSpatialSqlBuilder sqlBuilder;

    @Autowired
    JdbcClient jdbcClient;

    @Test
    void geoJsonRoundTrip() {
        String geoJson = jdbcClient.sql("select " + converter.toGeoJsonSql("geometry") + " from landmarks where id = :id")
                .param("id", 1L)
                .query(String.class)
                .single();

        assertThat(geoJson).contains("\"Point\"");
        assertThat(geoJson).contains("-122.3933");
    }

    @Test
    void spatialPredicatesWork() {
        String point = "{\"type\":\"Point\",\"coordinates\":[-122.4194,37.7749]}";
        String polygon = "{\"type\":\"Polygon\",\"coordinates\":[[[-122.53,37.70],[-122.35,37.70],[-122.35,37.83],[-122.53,37.83],[-122.53,37.70]]]}";

        Long filterCount = jdbcClient.sql("select count(*) from landmarks where " + sqlBuilder.filterPredicate("geometry", "shape"))
                .param("shape", polygon)
                .query(Long.class)
                .single();
        assertThat(filterCount).isGreaterThanOrEqualTo(2L);

        Long relateCount = jdbcClient.sql("select count(*) from landmarks where " + sqlBuilder.relatePredicate("geometry", "shape", "ANYINTERACT"))
                .param("shape", polygon)
                .query(Long.class)
                .single();
        assertThat(relateCount).isGreaterThanOrEqualTo(2L);

        Long withinDistanceCount = jdbcClient.sql("select count(*) from landmarks where "
                        + sqlBuilder.withinDistancePredicate("geometry", "shape", 2000))
                .param("shape", point)
                .query(Long.class)
                .single();
        assertThat(withinDistanceCount).isGreaterThanOrEqualTo(1L);

        String nearestName = jdbcClient.sql("select name from landmarks where "
                        + sqlBuilder.nearestNeighborPredicate("geometry", "shape", 1)
                        + " order by " + sqlBuilder.nearestNeighborDistance("geometry", "shape", "distance"))
                .param("shape", point)
                .query(String.class)
                .single();
        assertThat(nearestName).isEqualTo("Union Square");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(OracleSpatialAutoConfiguration.class)
    static class TestApplication {
    }
}
