/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle.autoconfigure;

import java.io.IOException;

import com.oracle.bmc.ClientRuntime;
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.RegionProvider;
import com.oracle.bmc.generativeaiinference.GenerativeAiInference;
import com.oracle.bmc.generativeaiinference.GenerativeAiInferenceClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

/**
 * Auto-configuration for OCI Generative AI Spring AI models.
 */
@AutoConfiguration
@ConditionalOnClass(GenerativeAiInference.class)
@EnableConfigurationProperties(AuthenticationProperties.class)
@Import({ ChatConfiguration.class, EmbeddingConfiguration.class })
public class GenAiAutoConfiguration {

    static final String USER_AGENT = "Oracle-SpringAI";

    @Bean
    @Conditional(GenAiModelSelectedCondition.class)
    @ConditionalOnMissingBean
    public BasicAuthenticationDetailsProvider oracleGenAiAuthenticationDetailsProvider(
            AuthenticationProperties properties) throws IOException {
        ClientRuntime.setClientUserAgent(USER_AGENT);
        return AuthenticationProviderFactory.create(properties);
    }

    @Bean
    @Conditional(GenAiModelSelectedCondition.class)
    @ConditionalOnMissingBean
    public GenerativeAiInference oracleGenAiInferenceClient(BasicAuthenticationDetailsProvider authenticationDetailsProvider,
            AuthenticationProperties properties) {
        GenerativeAiInference client = GenerativeAiInferenceClient.builder().build(authenticationDetailsProvider);
        if (StringUtils.hasText(properties.getEndpoint())) {
            client.setEndpoint(properties.getEndpoint());
        }
        else if (StringUtils.hasText(properties.getRegion())) {
            client.setRegion(properties.getRegion());
        }
        else if (authenticationDetailsProvider instanceof RegionProvider regionProvider
                && regionProvider.getRegion() != null) {
            client.setRegion(regionProvider.getRegion());
        }
        return client;
    }
}
