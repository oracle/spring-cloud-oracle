// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.spatial;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = OracleSpatialOverrideAutoConfigurationTest.TestApplication.class)
public class OracleSpatialOverrideAutoConfigurationTest {
    @Autowired
    OracleSpatialJdbcOperations spatial;

    @Test
    void userBeansTakePrecedence() {
        SpatialGeometry geometry = spatial.geometry("{\"type\":\"Point\",\"coordinates\":[-122.3893,37.7786]}");

        assertThat(geometry.srid()).isEqualTo(3857);
        assertThat(spatial.toGeoJson("geometry").expression()).isEqualTo("custom");
    }

    @TestConfiguration
    static class OverrideBeans {
        @Bean
        OracleSpatialJdbcOperations oracleSpatialJdbcOperations() {
            OracleSpatialProperties properties = new OracleSpatialProperties();
            properties.setDefaultSrid(3857);
            return new OracleSpatialJdbcOperations(properties) {
                @Override
                public SpatialExpression toGeoJson(String geometryColumn) {
                    return new SpatialExpression("custom", Map.of());
                }
            };
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({Config.class, OverrideBeans.class})
    static class TestApplication {
    }
}
