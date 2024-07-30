/*
 ** Copyright (c) 2024, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.genai;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.oracle.bmc.generativeaiinference.GenerativeAiInference;
import com.oracle.bmc.generativeaiinference.model.EmbedTextDetails;
import com.oracle.bmc.generativeaiinference.model.ServingMode;
import com.oracle.bmc.generativeaiinference.requests.EmbedTextRequest;
import com.oracle.bmc.generativeaiinference.responses.EmbedTextResponse;
import lombok.Builder;
import org.springframework.util.Assert;

/**
 * Implementation for OCI GenAI EmbedingModel
 */
public class EmbeddingModelImpl implements EmbeddingModel {

    /**
     * OCI GenAI accepts a maximum of 96 inputs per embedding request. If the input is greater
     * than 96 segments, it will be split into chunks of this size.
     */
    private static final int BATCH_SIZE = 96;

    private final GenerativeAiInference client;
    private final ServingMode servingMode;
    private final EmbedTextDetails.Truncate truncate;
    private final String compartment;

    @Builder
    public EmbeddingModelImpl(GenerativeAiInference client, ServingMode servingMode, EmbedTextDetails.Truncate truncate, String compartment) {
        Assert.notNull(client, "client must not be null");
        Assert.notNull(servingMode, "servingMode must not be null");
        Assert.notNull(compartment, "compartment must not be null");
        this.client = client;
        this.servingMode = servingMode;
        this.truncate = Optional.ofNullable(truncate).orElse(EmbedTextDetails.Truncate.None);
        this.compartment = compartment;
    }

    /**
     * Embeds a list of text inputs.
     * @param inputs Text inputs to embed.
     * @return The list of EmbedTextResponses for the input texts.
     */
    @Override
    public List<EmbedTextResponse> embedAll(List<String> inputs) {
        List<EmbedTextResponse> responses = new ArrayList<>();
        List<List<String>> batches = toBatches(inputs);
        for (List<String> batch : batches) {
            EmbedTextRequest request = toEmbedTextRequest(batch);
            EmbedTextResponse response = client.embedText(request);
            responses.add(response);
        }
        return responses;
    }

    /**
     * Splits a list of input texts into batches by the OCI GenAI embedding batch size.
     * @param inputs To split into batches.
     * @return A list of input text batches.
     */
    private List<List<String>> toBatches(List<String> inputs) {
        int size = inputs.size();
        List<List<String>> batches = new ArrayList<>();
        for (int i = 0; i < size; i+=BATCH_SIZE) {
            batches.add(inputs.subList(i, Math.min(i + BATCH_SIZE, size)));
        }
        return batches;
    }

    /**
     * Converts an input text batch into an OCI GenAI embedding request.
     * @param inputBatch For embedding.
     * @return An EmbedTextRequest containing the input batch.
     */
    private EmbedTextRequest toEmbedTextRequest(List<String> inputBatch) {
        EmbedTextDetails embedTextDetails = EmbedTextDetails.builder()
                .servingMode(servingMode)
                .compartmentId(compartment)
                .inputs(inputBatch)
                .truncate(truncate)
                .build();
        return EmbedTextRequest.builder().embedTextDetails(embedTextDetails).build();
    }
}
