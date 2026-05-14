/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle.autoconfigure;

import com.oracle.bmc.generativeaiinference.GenerativeAiInference;
import com.oracle.spring.ai.oracle.OracleGenAiChatModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.DefaultToolExecutionEligibilityPredicate;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionEligibilityPredicate;
import org.springframework.ai.retry.RetryUtils;
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
                                              ObjectProvider<RetryTemplate> retryTemplate) {
        return OracleGenAiChatModel.builder()
                .client(generativeAiInference)
                .defaultOptions(properties)
                .toolCallingManager(toolCallingManager.getIfAvailable(() -> ToolCallingManager.builder().build()))
                .toolExecutionEligibilityPredicate(toolExecutionEligibilityPredicate
                        .getIfAvailable(DefaultToolExecutionEligibilityPredicate::new))
                .retryTemplate(retryTemplate.getIfAvailable(() -> RetryUtils.DEFAULT_RETRY_TEMPLATE))
                .build();
    }
}
