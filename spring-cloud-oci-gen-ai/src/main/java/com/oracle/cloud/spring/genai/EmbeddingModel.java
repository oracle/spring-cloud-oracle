/*
 ** Copyright (c) 2024, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.genai;

import java.util.ArrayList;
import java.util.List;

import com.oracle.bmc.generativeaiinference.responses.EmbedTextResponse;

/**
 * Interface for embedding text with OCI GenAI Service.
 */
public interface EmbeddingModel {
    /**
     * Embeds a list of text inputs.
     * @param inputs Text inputs to embed.
     * @return The list of EmbedTextResponses for the input texts.
     */
    List<EmbedTextResponse> embedAll(List<String> inputs);
    /**
     * Embeds a single line of text.
     * @param text Text to embed.
     * @return The EmbedTextResponse for the input.
     */
    default EmbedTextResponse embed(String text) {
        return embedAll(List.of(text)).get(0);
    }
    default List<List<Float>> fromResponse(EmbedTextResponse response) {
        return fromResponses(List.of(response));
    }
    default List<List<Float>> fromResponses(List<EmbedTextResponse> responses) {
        List<List<Float>> embeddings = new ArrayList<>();
        for (EmbedTextResponse response : responses) {
            embeddings.addAll(response.getEmbedTextResult().getEmbeddings());
        }
        return embeddings;
    }
}
