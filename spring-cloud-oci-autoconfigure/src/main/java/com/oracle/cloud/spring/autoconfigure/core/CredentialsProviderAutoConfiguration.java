/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.autoconfigure.core;

import com.oracle.bmc.ClientRuntime;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ResourcePrincipalAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimplePrivateKeySupplier;
import com.oracle.bmc.auth.SessionTokenAuthenticationDetailsProvider;
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

    /**
     * Creates an CredentialsProvider based on {@link CredentialsProperties.ConfigType} type
     * @return CredentialsProvider
     * @throws IOException
     */
    @Bean (name = credentialsProviderQualifier)
    @RefreshScope
    @ConditionalOnMissingBean
    public CredentialsProvider credentialsProvider() throws IOException {
        return new CredentialsProvider(properties);
    }


}
