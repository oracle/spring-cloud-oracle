/*
 ** Copyright (c) 2024, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.genai;

import com.oracle.bmc.auth.RegionProvider;
import com.oracle.bmc.generativeaiinference.GenerativeAiInference;
import com.oracle.bmc.generativeaiinference.GenerativeAiInferenceClient;
import com.oracle.bmc.generativeaiinference.model.DedicatedServingMode;
import com.oracle.bmc.generativeaiinference.model.EmbedTextDetails;
import com.oracle.bmc.generativeaiinference.model.OnDemandServingMode;
import com.oracle.bmc.generativeaiinference.model.ServingMode;
import com.oracle.cloud.spring.autoconfigure.core.CredentialsProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import static com.oracle.cloud.spring.autoconfigure.core.CredentialsProviderAutoConfiguration.credentialsProviderQualifier;
import static com.oracle.cloud.spring.autoconfigure.core.RegionProviderAutoConfiguration.regionProviderQualifier;

/**
 * Auto-configuration for initializing the OCI GenAI component.
 *  Depends on {@link com.oracle.cloud.spring.autoconfigure.core.CredentialsProviderAutoConfiguration} and
 *  {@link com.oracle.cloud.spring.autoconfigure.core.RegionProviderAutoConfiguration}
 *  for loading the Authentication configuration
 *
 * @see ChatModel
 * @see EmbeddingModel
 */
@AutoConfiguration
@ConditionalOnClass({ChatModel.class})
@EnableConfigurationProperties(GenAIProperties.class)
@ConditionalOnProperty(name = "spring.cloud.oci.genai.enabled", havingValue = "true", matchIfMissing = true)
public class GenAIAutoConfiguration {
    private final GenAIProperties properties;

    public GenAIAutoConfiguration(GenAIProperties properties) {
        this.properties = properties;
    }

    @Bean
    @RefreshScope
    @ConditionalOnProperty(name = "spring.cloud.oci.genai.embedding.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(EmbeddingModel.class)
    public EmbeddingModel embeddingModel(GenerativeAiInference generativeAiInference) {
        GenAIProperties.Embedding embedding = properties.getEmbedding();
        return EmbeddingModelImpl.builder()
                .client(generativeAiInference)
                .truncate(StringUtils.hasText(embedding.getTruncate()) ?
                        EmbedTextDetails.Truncate.valueOf(embedding.getTruncate()) :
                        EmbedTextDetails.Truncate.None)
                .compartment(embedding.getCompartment())
                .servingMode(servingMode(embedding.getOnDemandModelId(), embedding.getDedicatedClusterEndpoint()))
                .build();
    }

    @Bean
    @RefreshScope
    @ConditionalOnProperty(name = "spring.cloud.oci.genai.chat.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(ChatModel.class)
    public ChatModel chatModel(GenerativeAiInference generativeAiInference) {
        GenAIProperties.Chat chat = properties.getChat();
        return ChatModelImpl.builder()
                .client(generativeAiInference)
                .preambleOverride(chat.getPreambleOverride())
                .inferenceRequestType(chat.getInferenceRequestType())
                .servingMode(servingMode(chat.getOnDemandModelId(), chat.getDedicatedClusterEndpoint()))
                .topK(chat.getTopK())
                .topP(chat.getTopP())
                .compartment(chat.getCompartment())
                .frequencyPenalty(chat.getFrequencyPenalty())
                .presencePenalty(chat.getPresencePenalty())
                .temperature(chat.getTemperature())
                .build();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean
    GenerativeAiInference genAIClient(@Qualifier(regionProviderQualifier) RegionProvider regionProvider,
                                                @Qualifier(credentialsProviderQualifier)
                                                CredentialsProvider cp) {
        GenerativeAiInference generativeAiInference = GenerativeAiInferenceClient.builder()
                .build(cp.getAuthenticationDetailsProvider());
        if (regionProvider.getRegion() != null) {
            generativeAiInference.setRegion(regionProvider.getRegion());
        }
        return generativeAiInference;
    }

    private ServingMode servingMode(String onDemandModelId, String dedicatedClusterEndpoint) {
        if (StringUtils.hasText(onDemandModelId)) {
            return OnDemandServingMode.builder().modelId(onDemandModelId).build();
        } else if (StringUtils.hasText(dedicatedClusterEndpoint)) {
            return DedicatedServingMode.builder().endpointId(dedicatedClusterEndpoint).build();
        }
        throw new IllegalArgumentException("One of spring.cloud.oci.genai.embedding.onDemandModelId or spring.cloud.oci.genai.embedding.dedicatedClusterEndpoint must be specified.");
    }
}
