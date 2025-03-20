/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.autoconfigure;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.RegionProvider;
import com.oracle.cloud.spring.autoconfigure.core.CredentialsProperties;
import com.oracle.cloud.spring.core.compartment.CompartmentProvider;
import com.oracle.cloud.spring.core.compartment.StaticCompartmentProvider;
import com.oracle.cloud.spring.core.region.StaticRegionProvider;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@Configuration
public class TestCommonConfigurationBeans {
    @Bean
    CredentialsProperties credentialsProperties() {
        return new CredentialsProperties();
    }

    @Bean
    BasicAuthenticationDetailsProvider credentialsProvider() {
        return mock(BasicAuthenticationDetailsProvider.class);
    }

    @Bean
    RegionProvider regionProvider() {
        return new StaticRegionProvider(Region.US_PHOENIX_1.getRegionId());
    }

    @Bean
    CompartmentProvider compartmentProvider() {
        return new StaticCompartmentProvider("compartmentOCID");
    }

    public static void assertBeanLoaded(AssertableApplicationContext ctx, Class<?> clazz) {
        assertBeanLoaded(ctx, clazz, true);
    }

    public static void assertBeanNotLoaded(AssertableApplicationContext ctx, Class<?> clazz) {
        assertBeanLoaded(ctx, clazz, false);
    }

    static void assertBeanLoaded(AssertableApplicationContext ctx, Class<?> clazz, boolean loaded) {
        String[] beans = ctx.getBeanNamesForType(clazz);
        assertThat(beans).hasSize(loaded ? 2 : 0);
    }

}
