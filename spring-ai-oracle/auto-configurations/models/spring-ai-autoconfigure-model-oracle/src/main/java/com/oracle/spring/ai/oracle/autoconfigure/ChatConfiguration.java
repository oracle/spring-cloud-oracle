/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle.autoconfigure;

import com.oracle.bmc.generativeaiinference.GenerativeAiInference;
import com.oracle.spring.ai.oracle.OracleGenAiChatModel;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionEligibilityPredicate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryTemplate;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(OracleGenAiChatModel.class)
@ConditionalOnProperty(name = PropertyNames.CHAT_MODEL_PROPERTY,
        havingValue = PropertyNames.MODEL_VALUE, matchIfMissing = true)
@EnableConfigurationProperties(ChatProperties.class)
class ChatConfiguration {

    @Bean
    @ConditionalOnMissingBean(ChatModel.class)
    OracleGenAiChatModel oracleGenAiChatModel(GenerativeAiInference generativeAiInference,
                                              ChatProperties properties,
                                              ObjectProvider<ToolCallingManager> toolCallingManager,
                                              ObjectProvider<ToolExecutionEligibilityPredicate> toolExecutionEligibilityPredicate,
                                              ObjectProvider<RetryTemplate> retryTemplate,
                                              ObjectProvider<ObservationRegistry> observationRegistry,
                                              ObjectProvider<ChatModelObservationConvention> observationConvention) {
        return OracleGenAiChatModel.builder()
                .client(generativeAiInference)
                .defaultOptions(properties)
                .toolCallingManager(toolCallingManager.getIfAvailable())
                .toolExecutionEligibilityPredicate(toolExecutionEligibilityPredicate.getIfAvailable())
                .retryTemplate(retryTemplate.getIfAvailable())
                .observationRegistry(observationRegistry.getIfAvailable())
                .observationConvention(observationConvention.getIfAvailable())
                .build();
    }
}
