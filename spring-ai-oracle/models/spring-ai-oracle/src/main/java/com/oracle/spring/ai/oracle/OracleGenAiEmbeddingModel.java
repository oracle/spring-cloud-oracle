/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.oracle.bmc.generativeaiinference.GenerativeAiInference;
import com.oracle.bmc.generativeaiinference.model.EmbedTextDetails;
import com.oracle.bmc.generativeaiinference.model.EmbedTextResult;
import com.oracle.bmc.generativeaiinference.model.Usage;
import com.oracle.bmc.generativeaiinference.requests.EmbedTextRequest;
import com.oracle.bmc.generativeaiinference.responses.EmbedTextResponse;
import com.oracle.spring.ai.oracle.api.OracleGenAiEmbeddingTruncate;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Spring AI embedding model backed by OCI Generative AI.
 */
public class OracleGenAiEmbeddingModel implements EmbeddingModel {

    private final GenerativeAiInference client;

    private final OracleGenAiEmbeddingOptions defaultOptions;

    private final RetryTemplate retryTemplate;

    public OracleGenAiEmbeddingModel(GenerativeAiInference client, OracleGenAiEmbeddingOptions defaultOptions) {
        this(client, defaultOptions, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public OracleGenAiEmbeddingModel(GenerativeAiInference client, OracleGenAiEmbeddingOptions defaultOptions,
            RetryTemplate retryTemplate) {
        Assert.notNull(client, "client must not be null");
        Assert.notNull(defaultOptions, "defaultOptions must not be null");
        Assert.notNull(retryTemplate, "retryTemplate must not be null");
        this.client = client;
        this.defaultOptions = defaultOptions.copy();
        this.retryTemplate = retryTemplate;
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        Assert.notNull(request, "request must not be null");
        OracleGenAiEmbeddingOptions options = defaultOptions.merge(request.getOptions());
        EmbedTextRequest ociRequest = toEmbedTextRequest(request.getInstructions(), options);
        EmbedTextResponse response = RetryUtils.execute(retryTemplate, () -> client.embedText(ociRequest));
        return toEmbeddingResponse(response);
    }

    @Override
    public float[] embed(Document document) {
        Assert.notNull(document, "document must not be null");
        return embed(getEmbeddingContent(document));
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
}
