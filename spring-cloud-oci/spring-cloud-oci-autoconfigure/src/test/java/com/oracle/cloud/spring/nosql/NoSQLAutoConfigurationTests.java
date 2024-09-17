// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.cloud.spring.nosql;

import com.oracle.cloud.spring.autoconfigure.TestCommonConfigurationBeans;
import com.oracle.nosql.spring.data.config.NosqlDbConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.oracle.cloud.spring.autoconfigure.TestCommonConfigurationBeans.assertBeanLoaded;
import static com.oracle.cloud.spring.autoconfigure.TestCommonConfigurationBeans.assertBeanNotLoaded;

public class NoSQLAutoConfigurationTests {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("spring.cloud.oci.nosql.enabled=true")
            .withConfiguration(AutoConfigurations.of(NoSQLAutoConfiguration.class))
            .withUserConfiguration(TestCommonConfigurationBeans.class);

    @Test
    void beansAreLoaded() {
        contextRunner.run(ctx -> assertBeanLoaded(ctx, NosqlDbConfig.class));
    }

    @Test
    void beansAreNotLoadedWhenDisabled() {
        contextRunner.withPropertyValues("spring.cloud.oci.nosql.enabled=false")
                .run(ctx -> assertBeanNotLoaded(ctx, NosqlDbConfig.class));
    }
}
