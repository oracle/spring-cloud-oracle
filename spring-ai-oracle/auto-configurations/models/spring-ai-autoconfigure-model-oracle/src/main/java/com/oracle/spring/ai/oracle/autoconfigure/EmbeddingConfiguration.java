/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle.autoconfigure;

import com.oracle.bmc.generativeaiinference.GenerativeAiInference;
import com.oracle.spring.ai.oracle.OracleGenAiEmbeddingModel;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationConvention;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryTemplate;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(OracleGenAiEmbeddingModel.class)
@ConditionalOnProperty(name = PropertyNames.EMBEDDING_MODEL_PROPERTY,
        havingValue = PropertyNames.MODEL_VALUE, matchIfMissing = true)
@EnableConfigurationProperties(EmbeddingProperties.class)
class EmbeddingConfiguration {

    @Bean
    @ConditionalOnMissingBean(EmbeddingModel.class)
    OracleGenAiEmbeddingModel oracleGenAiEmbeddingModel(GenerativeAiInference generativeAiInference,
                                                        EmbeddingProperties properties,
                                                        ObjectProvider<RetryTemplate> retryTemplate,
                                                        ObjectProvider<ObservationRegistry> observationRegistry,
                                                        ObjectProvider<EmbeddingModelObservationConvention> observationConvention) {
        return OracleGenAiEmbeddingModel.builder()
                .client(generativeAiInference)
                .defaultOptions(properties)
                .retryTemplate(retryTemplate.getIfAvailable())
                .observationRegistry(observationRegistry.getIfAvailable())
                .observationConvention(observationConvention.getIfAvailable())
                .build();
    }
}
