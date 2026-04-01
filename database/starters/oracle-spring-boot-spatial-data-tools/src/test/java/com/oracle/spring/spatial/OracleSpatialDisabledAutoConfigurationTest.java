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

@SpringBootTest(
        classes = OracleSpatialDisabledAutoConfigurationTest.TestApplication.class,
        properties = "oracle.database.spatial.enabled=false"
)
public class OracleSpatialDisabledAutoConfigurationTest {
    @Autowired(required = false)
    OracleSpatialJdbcOperations spatial;

    @Test
    void spatialBeansDisabled() {
        assertThat(spatial).isNull();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(Config.class)
    static class TestApplication {
    }
}
