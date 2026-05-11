/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle.autoconfigure;

import java.io.IOException;

import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.RegionProvider;
import com.oracle.bmc.generativeaiinference.GenerativeAiInference;
import com.oracle.bmc.generativeaiinference.GenerativeAiInferenceClient;
import com.oracle.spring.ai.oracle.OracleGenAiChatModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.util.StringUtils;

/**
 * Auto-configuration for OCI Generative AI Spring AI chat model.
 */
@AutoConfiguration
@ConditionalOnClass({ OracleGenAiChatModel.class, GenerativeAiInference.class })
@ConditionalOnProperty(name = "spring.ai.model.chat", havingValue = "oracle")
@EnableConfigurationProperties({ OracleGenAiChatProperties.class, OracleGenAiAuthenticationProperties.class })
public class OracleGenAiChatAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BasicAuthenticationDetailsProvider oracleGenAiAuthenticationDetailsProvider(
            OracleGenAiAuthenticationProperties properties) throws IOException {
        return properties.createBasicAuthenticationDetailsProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public GenerativeAiInference oracleGenAiInferenceClient(BasicAuthenticationDetailsProvider authenticationDetailsProvider,
            OracleGenAiAuthenticationProperties properties) {
        GenerativeAiInference client = GenerativeAiInferenceClient.builder().build(authenticationDetailsProvider);
        if (StringUtils.hasText(properties.getRegion())) {
            client.setRegion(properties.getRegion());
        }
        else if (authenticationDetailsProvider instanceof RegionProvider regionProvider
                && regionProvider.getRegion() != null) {
            client.setRegion(regionProvider.getRegion());
        }
        return client;
    }

    @Bean
    @ConditionalOnMissingBean(ChatModel.class)
    public OracleGenAiChatModel oracleGenAiChatModel(GenerativeAiInference generativeAiInference,
            OracleGenAiChatProperties properties, ObjectProvider<RetryTemplate> retryTemplate) {
        return new OracleGenAiChatModel(generativeAiInference, properties.getOptions(),
                retryTemplate.getIfAvailable(() -> RetryUtils.DEFAULT_RETRY_TEMPLATE));
    }
}
