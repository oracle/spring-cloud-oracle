/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.logging;

import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.RegionProvider;
import com.oracle.bmc.loggingingestion.LoggingClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for initializing the OCI Logging component.
 *  Depends on {@link com.oracle.cloud.spring.autoconfigure.core.CredentialsProviderAutoConfiguration} and
 *  {@link com.oracle.cloud.spring.autoconfigure.core.RegionProviderAutoConfiguration}
 *  for loading the Authentication configuration
 *
 * @see Logging
 */
@AutoConfiguration
@ConditionalOnClass({Logging.class})
@EnableConfigurationProperties(LoggingProperties.class)
@ConditionalOnProperty(name = "spring.cloud.oci.logging.enabled", havingValue = "true", matchIfMissing = true)
public class LoggingAutoConfiguration {

    private final LoggingProperties properties;

    public LoggingAutoConfiguration(LoggingProperties properties) {
        this.properties = properties;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(Logging.class)
    Logging getLoggingImpl(com.oracle.bmc.loggingingestion.Logging logging) {
        return new LoggingImpl(logging, properties.getLogId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean
    com.oracle.bmc.loggingingestion.Logging loggingClient(RegionProvider regionProvider,
                                                          BasicAuthenticationDetailsProvider adp) {
        com.oracle.bmc.loggingingestion.Logging logging = new LoggingClient(adp);
        if (regionProvider.getRegion() != null) logging.setRegion(regionProvider.getRegion());
        return logging;
    }
}
