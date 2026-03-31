// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.spatial;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/init.sql")
public class SpatialSampleApplicationTest {
    @Container
    static OracleContainer oracleContainer = new OracleContainer("gvenzl/oracle-free:23.26.0-slim-faststart")
            .withStartupTimeout(Duration.ofMinutes(2))
            .withUsername("testuser")
            .withPassword("testpwd");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("JDBC_URL", oracleContainer::getJdbcUrl);
        registry.add("USERNAME", oracleContainer::getUsername);
        registry.add("PASSWORD", oracleContainer::getPassword);
    }

    @LocalServerPort
    int port;

    @Test
    void spatialSampleApp() {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        Landmark inserted = new Landmark(
                10L,
                "Oracle Park",
                "STADIUM",
                "{\"type\":\"Point\",\"coordinates\":[-122.3893,37.7786]}"
        );

        Landmark created = restClient.post()
                .uri("/landmarks")
                .body(inserted)
                .retrieve()
                .body(Landmark.class);
        assertThat(created).isNotNull();
        assertThat(created.name()).isEqualTo("Oracle Park");
        assertThat(created.geometry()).contains("\"Point\"");

        Landmark fetched = restClient.get()
                .uri("/landmarks/10")
                .retrieve()
                .body(Landmark.class);
        assertThat(fetched).isNotNull();
        assertThat(fetched.category()).isEqualTo("STADIUM");

        Landmark[] nearResults = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/landmarks/near")
                        .queryParam("geometry", "{\"type\":\"Point\",\"coordinates\":[-122.3933,37.7955]}")
                        .queryParam("distance", 5000)
                        .queryParam("limit", 2)
                        .build())
                .retrieve()
                .body(Landmark[].class);
        assertThat(nearResults).isNotNull();
        assertThat(List.of(nearResults)).isNotEmpty();

        WithinLandmarkRequest withinRequest = new WithinLandmarkRequest(
                "{\"type\":\"Polygon\",\"coordinates\":[[[-122.53,37.70],[-122.35,37.70],[-122.35,37.83],[-122.53,37.83],[-122.53,37.70]]]}",
                "ANYINTERACT"
        );
        Landmark[] withinResults = restClient.post()
                .uri("/landmarks/within")
                .body(withinRequest)
                .retrieve()
                .body(Landmark[].class);
        assertThat(withinResults).isNotNull();
        assertThat(List.of(withinResults))
                .extracting(Landmark::name)
                .contains("Ferry Building", "Union Square", "Oracle Park");
    }
}
