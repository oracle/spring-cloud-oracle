/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.autoconfigure.core;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.RegionProvider;
import com.oracle.cloud.spring.core.region.StaticRegionProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for initializing the OCI Region
 */
@AutoConfiguration
@ConditionalOnClass({AuthenticationDetailsProvider.class})
@EnableConfigurationProperties(RegionProperties.class)
public class RegionProviderAutoConfiguration {

    private final RegionProperties properties;

    public RegionProviderAutoConfiguration(RegionProperties properties) {
        this.properties = properties;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean
    public RegionProvider regionProvider() {
        return createRegionProvider(properties);
    }

    public static RegionProvider createRegionProvider(RegionProperties properties) {
        if (properties.getStatic() != null && properties.isStatic()) {
            return new StaticRegionProvider(properties.getStatic().trim());
        }

        return new StaticRegionProvider();
    }
}
