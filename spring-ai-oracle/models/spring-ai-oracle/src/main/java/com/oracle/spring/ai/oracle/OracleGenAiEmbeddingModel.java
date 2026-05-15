/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.oracle.bmc.generativeaiinference.GenerativeAiInference;
import com.oracle.bmc.generativeaiinference.model.EmbedTextDetails;
import com.oracle.bmc.generativeaiinference.model.EmbedTextResult;
import com.oracle.bmc.generativeaiinference.model.Usage;
import com.oracle.bmc.generativeaiinference.requests.EmbedTextRequest;
import com.oracle.bmc.generativeaiinference.responses.EmbedTextResponse;
import com.oracle.spring.ai.oracle.api.OracleGenAiEmbeddingTruncate;
import io.micrometer.observation.ObservationRegistry;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;
import org.springframework.ai.embedding.observation.DefaultEmbeddingModelObservationConvention;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationContext;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationConvention;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationDocumentation;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Spring AI embedding model backed by OCI Generative AI.
 */
public class OracleGenAiEmbeddingModel implements EmbeddingModel {

    private static final EmbeddingModelObservationConvention DEFAULT_OBSERVATION_CONVENTION =
            new DefaultEmbeddingModelObservationConvention();

    private final GenerativeAiInference client;

    private final OracleGenAiEmbeddingOptions defaultOptions;

    private final RetryTemplate retryTemplate;

    private final ObservationRegistry observationRegistry;

    private EmbeddingModelObservationConvention observationConvention = DEFAULT_OBSERVATION_CONVENTION;

    public static Builder builder() {
        return new Builder();
    }

    private OracleGenAiEmbeddingModel(@Nullable GenerativeAiInference client,
            @Nullable OracleGenAiEmbeddingOptions defaultOptions, @Nullable RetryTemplate retryTemplate,
            @Nullable ObservationRegistry observationRegistry,
            @Nullable EmbeddingModelObservationConvention observationConvention) {
        this.client = Objects.requireNonNull(client, "client must not be null");
        this.defaultOptions = Objects.requireNonNull(defaultOptions, "defaultOptions must not be null").copy();
        this.retryTemplate = Objects.requireNonNullElse(retryTemplate, RetryUtils.DEFAULT_RETRY_TEMPLATE);
        this.observationRegistry = Objects.requireNonNullElse(observationRegistry, ObservationRegistry.NOOP);
        this.observationConvention = Objects.requireNonNullElse(observationConvention, DEFAULT_OBSERVATION_CONVENTION);
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        Assert.notNull(request, "request must not be null");
        OracleGenAiEmbeddingOptions options = defaultOptions.merge(request.getOptions());
        EmbeddingRequest embeddingRequest = new EmbeddingRequest(request.getInstructions(), options);
        EmbedTextRequest ociRequest = toEmbedTextRequest(embeddingRequest.getInstructions(), options);
        EmbeddingModelObservationContext observationContext = EmbeddingModelObservationContext.builder()
                .embeddingRequest(embeddingRequest)
                .provider(OracleGenAiModelMetadata.PROVIDER)
                .build();
        return EmbeddingModelObservationDocumentation.EMBEDDING_MODEL_OPERATION
                .observation(observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
                        observationRegistry)
                .observe(() -> {
                    EmbedTextResponse response = RetryUtils.execute(retryTemplate, () -> client.embedText(ociRequest));
                    EmbeddingResponse embeddingResponse = toEmbeddingResponse(response);
                    observationContext.setResponse(embeddingResponse);
                    return embeddingResponse;
                });
    }

    @Override
    public float[] embed(Document document) {
        Assert.notNull(document, "document must not be null");
        return embed(getEmbeddingContent(document));
    }

    public void setObservationConvention(EmbeddingModelObservationConvention observationConvention) {
        Assert.notNull(observationConvention, "observationConvention must not be null");
        this.observationConvention = observationConvention;
    }

    EmbedTextRequest toEmbedTextRequest(List<String> inputs, OracleGenAiEmbeddingOptions options) {
        options.validate();
        validateInputs(inputs);
        EmbedTextDetails details = EmbedTextDetails.builder()
                .compartmentId(options.getCompartmentId())
                .servingMode(options.toServingMode())
                .inputs(inputs)
                .outputDimensions(options.getDimensions())
                .truncate(toTruncate(options.getTruncate()))
                .build();
        return EmbedTextRequest.builder().embedTextDetails(details).build();
    }

    private EmbeddingResponse toEmbeddingResponse(EmbedTextResponse response) {
        EmbedTextResult result = response.getEmbedTextResult();
        if (result == null) {
            throw new IllegalStateException("OCI Generative AI embedding response did not contain an embedding result.");
        }
        if (CollectionUtils.isEmpty(result.getEmbeddings())) {
            throw new IllegalStateException("OCI Generative AI embedding response did not contain embeddings.");
        }
        List<Embedding> embeddings = new ArrayList<>(result.getEmbeddings().size());
        for (int index = 0; index < result.getEmbeddings().size(); index++) {
            embeddings.add(new Embedding(toFloatArray(result.getEmbeddings().get(index), index), index));
        }
        return new EmbeddingResponse(embeddings, toMetadata(response, result));
    }

    private static EmbeddingResponseMetadata toMetadata(EmbedTextResponse response, EmbedTextResult result) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        addIfPresent(metadata, "id", result.getId());
        addIfPresent(metadata, "modelVersion", result.getModelVersion());
        addIfPresent(metadata, "opcRequestId", response.getOpcRequestId());
        addIfPresent(metadata, "modelDeprecationInfo", response.getModelDeprecationInfo());
        return new EmbeddingResponseMetadata(result.getModelId(), toUsage(result.getUsage()), metadata);
    }

    private static float[] toFloatArray(List<Float> vector, int index) {
        if (vector == null) {
            throw new IllegalStateException("OCI Generative AI embedding response contained a null embedding at index "
                    + index + ".");
        }
        float[] result = new float[vector.size()];
        for (int i = 0; i < vector.size(); i++) {
            Float value = vector.get(i);
            if (value == null) {
                throw new IllegalStateException("OCI Generative AI embedding response contained a null value at embedding "
                        + index + ", vector position " + i + ".");
            }
            result[i] = value;
        }
        return result;
    }

    private static DefaultUsage toUsage(Usage usage) {
        if (usage == null) {
            return new DefaultUsage(0, 0, 0);
        }
        return new DefaultUsage(usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens(), usage);
    }

    private static void addIfPresent(Map<String, Object> metadata, String name, Object value) {
        if (value != null) {
            metadata.put(name, value);
        }
    }

    private static void validateInputs(List<String> inputs) {
        if (CollectionUtils.isEmpty(inputs)) {
            throw new IllegalArgumentException("Embedding inputs must not be empty.");
        }
        for (String input : inputs) {
            if (!StringUtils.hasText(input)) {
                throw new IllegalArgumentException("Embedding input text must not be empty.");
            }
        }
    }

    private static EmbedTextDetails.Truncate toTruncate(OracleGenAiEmbeddingTruncate truncate) {
        if (truncate == null || truncate == OracleGenAiEmbeddingTruncate.NONE) {
            return EmbedTextDetails.Truncate.None;
        }
        if (truncate == OracleGenAiEmbeddingTruncate.START) {
            return EmbedTextDetails.Truncate.Start;
        }
        return EmbedTextDetails.Truncate.End;
    }

    public static final class Builder {

        @Nullable
        private GenerativeAiInference client;

        @Nullable
        private OracleGenAiEmbeddingOptions defaultOptions;

        @Nullable
        private RetryTemplate retryTemplate;

        @Nullable
        private ObservationRegistry observationRegistry;

        @Nullable
        private EmbeddingModelObservationConvention observationConvention;

        private Builder() {
        }

        public Builder client(GenerativeAiInference client) {
            this.client = client;
            return this;
        }

        public Builder defaultOptions(OracleGenAiEmbeddingOptions defaultOptions) {
            this.defaultOptions = defaultOptions;
            return this;
        }

        public Builder retryTemplate(RetryTemplate retryTemplate) {
            this.retryTemplate = retryTemplate;
            return this;
        }

        public Builder observationRegistry(ObservationRegistry observationRegistry) {
            this.observationRegistry = observationRegistry;
            return this;
        }

        public Builder observationConvention(EmbeddingModelObservationConvention observationConvention) {
            this.observationConvention = observationConvention;
            return this;
        }

        public OracleGenAiEmbeddingModel build() {
            return new OracleGenAiEmbeddingModel(client, defaultOptions, retryTemplate, observationRegistry,
                    observationConvention);
        }
    }
}
