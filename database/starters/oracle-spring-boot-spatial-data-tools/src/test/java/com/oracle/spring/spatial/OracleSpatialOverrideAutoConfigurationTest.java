// Copyright (c) 2026, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.spring.spatial;

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
    OracleSpatialGeoJsonConverter converter;

    @Autowired
    OracleSpatialSqlBuilder sqlBuilder;

    @Test
    void userBeansTakePrecedence() {
        assertThat(converter.defaultSrid()).isEqualTo(3857);
        assertThat(sqlBuilder.geometryToGeoJson("geometry")).isEqualTo("custom");
    }

    @TestConfiguration
    static class OverrideBeans {
        @Bean
        OracleSpatialGeoJsonConverter oracleSpatialGeoJsonConverter() {
            OracleSpatialProperties properties = new OracleSpatialProperties();
            properties.setDefaultSrid(3857);
            return new OracleSpatialGeoJsonConverter(properties);
        }

        @Bean
        OracleSpatialSqlBuilder oracleSpatialSqlBuilder() {
            return new OracleSpatialSqlBuilder(new OracleSpatialGeoJsonConverter(new OracleSpatialProperties())) {
                @Override
                public String geometryToGeoJson(String geometryExpression) {
                    return "custom";
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
