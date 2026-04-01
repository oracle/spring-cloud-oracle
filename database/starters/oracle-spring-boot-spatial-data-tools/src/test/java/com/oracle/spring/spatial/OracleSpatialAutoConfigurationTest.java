// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.spatial;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = OracleSpatialAutoConfigurationTest.TestApplication.class)
public class OracleSpatialAutoConfigurationTest {
    @Autowired
    OracleSpatialJdbcOperations spatial;

    @Autowired
    OracleSpatialProperties properties;

    @Test
    void spatialBeansConfigured() {
        SpatialGeometry geometry = spatial.geometry("{\"type\":\"Point\",\"coordinates\":[-122.3933,37.7955]}");
        SpatialExpression fromGeoJson = spatial.fromGeoJson(geometry);
        SpatialPredicate withinDistance = spatial.withinDistance("geometry", geometry, 500);

        assertThat(spatial).isNotNull();
        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getDefaultSrid()).isEqualTo(4326);
        assertThat(properties.getDefaultDistanceUnit()).isEqualTo("M");
        assertThat(fromGeoJson.expression()).isEqualTo("SDO_UTIL.FROM_GEOJSON(:spatialGeometry1, null, 4326)");
        assertThat(withinDistance.clause())
                .contains("SDO_WITHIN_DISTANCE")
                .contains("distance=500 unit=M");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(Config.class)
    static class TestApplication {
    }
}
