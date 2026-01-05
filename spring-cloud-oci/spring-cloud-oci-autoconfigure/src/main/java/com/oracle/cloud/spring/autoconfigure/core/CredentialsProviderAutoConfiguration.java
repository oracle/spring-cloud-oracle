/*
 ** Copyright (c) 2023, 2026, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.autoconfigure.core;

import com.oracle.bmc.auth.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

/**
 * Auto-configuration for initializing the OCI Credentials provider type
 */
@AutoConfiguration
@ConditionalOnClass({AuthenticationDetailsProvider.class})
@EnableConfigurationProperties(CredentialsProperties.class)
public class CredentialsProviderAutoConfiguration {

    public static final String credentialsProviderQualifier = "credentialsProvider";

    private final CredentialsProperties properties;

    public CredentialsProviderAutoConfiguration(CredentialsProperties properties) {
        this.properties = properties;
    }


    @Bean
    public BasicAuthenticationDetailsProvider basicAuthenticationDetailsProvider() throws IOException {
        return properties.createBasicAuthenticationDetailsProvider();
    }

    /**
     * Creates an CredentialsProvider based on {@link CredentialsProperties.ConfigType} type
     * @return CredentialsProvider
     * @throws IOException
     */
    @Bean (name = credentialsProviderQualifier)
    @RefreshScope
    @ConditionalOnMissingBean
    public CredentialsProvider credentialsProvider(BasicAuthenticationDetailsProvider authenticationDetailsProvider) throws IOException {
        return new CredentialsProvider(authenticationDetailsProvider);
    }
}
