/*
 ** Copyright (c) 2023, 2026, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.autoconfigure.core;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.RegionProvider;
import com.oracle.cloud.spring.core.region.StaticRegionProvider;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

/**
 * Auto-configuration for initializing the OCI Region
 */
@AutoConfiguration
@ConditionalOnClass({AuthenticationDetailsProvider.class})
@EnableConfigurationProperties(RegionProperties.class)
@SuppressFBWarnings(value = "EI2",
        justification = "Configuration properties are managed by Spring and intentionally retained by auto-configuration.")
public class RegionProviderAutoConfiguration {

    public static final String regionProviderQualifier = "regionProvider";
    private final RegionProperties properties;

    public RegionProviderAutoConfiguration(RegionProperties properties) {
        this.properties = properties;
    }

    @Bean (name = regionProviderQualifier)
    @RefreshScope
    @ConditionalOnMissingBean
    public RegionProvider regionProvider() {
        return createRegionProvider(properties);
    }

    public static RegionProvider createRegionProvider(RegionProperties properties) {
        String staticRegion = properties.getStatic();
        if (StringUtils.hasText(staticRegion)) {
            return new StaticRegionProvider(staticRegion.trim());
        }

        return new StaticRegionProvider();
    }
}
