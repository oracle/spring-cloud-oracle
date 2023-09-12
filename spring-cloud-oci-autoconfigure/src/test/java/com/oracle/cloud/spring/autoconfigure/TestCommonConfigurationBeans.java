/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.autoconfigure;

import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.RegionProvider;
import com.oracle.cloud.spring.core.compartment.CompartmentProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@Configuration
public class TestCommonConfigurationBeans {
    @Bean
    BasicAuthenticationDetailsProvider credentialsProvider() {
        return mock(BasicAuthenticationDetailsProvider.class);
    }

    @Bean
    RegionProvider regionProvider() {
        return mock(RegionProvider.class);
    }

    @Bean
    CompartmentProvider compartmentProvider() {
        return mock(CompartmentProvider.class);
    }

}
