/*
 ** Copyright (c) 2024, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.genai;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import com.oracle.bmc.generativeaiinference.GenerativeAiInference;
import com.oracle.bmc.generativeaiinference.model.EmbedTextDetails;
import com.oracle.bmc.generativeaiinference.model.EmbedTextResult;
import com.oracle.bmc.generativeaiinference.model.OnDemandServingMode;
import com.oracle.bmc.generativeaiinference.responses.EmbedTextResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EmbeddingModelTest {

    @Test
    void fromResponse() {
        EmbeddingModel model = testEmbeddingModel(mock(GenerativeAiInference.class));
        List<List<Float>> embeddings = model.fromResponse(fromVectors(3));
        Assertions.assertEquals(embeddings.size(), 3);
    }

    @Test
    void fromResponses() {
        EmbeddingModel model = testEmbeddingModel(mock(GenerativeAiInference.class));
        List<List<Float>> embeddings = model.fromResponses(List.of(fromVectors(3), fromVectors(4)));
        Assertions.assertEquals(embeddings.size(), 7);
    }

    @Test
    void embed() throws Exception {
        try (GenerativeAiInference mockClient = mock(GenerativeAiInference.class)) {
            EmbeddingModel model = testEmbeddingModel(mockClient);
            when(mockClient.embedText(any())).thenReturn(fromVectors(1));
            EmbedTextResponse embedTextResponse = model.embed("The bearded iris is a type of flower.");
            Assertions.assertEquals(model.fromResponse(embedTextResponse).size(), 1);
        }
    }

    @Test
    void embedAll() throws Exception {
        try (GenerativeAiInference mockClient = mock(GenerativeAiInference.class)) {
            EmbeddingModel model = testEmbeddingModel(mockClient);
            when(mockClient.embedText(any())).thenReturn(fromVectors(2));
            List<EmbedTextResponse> embedTextResponses = model.embedAll(List.of("There are 50 states in the USA", "There are 10 provinces in Canada"));
            Assertions.assertEquals(embedTextResponses.size(), 1);
            Assertions.assertEquals(model.fromResponses(embedTextResponses).size(), 2);
        }
    }

    @Test
    void embedAllWithBatching() throws Exception {
        try (GenerativeAiInference mockClient = mock(GenerativeAiInference.class)) {
            EmbeddingModel model = testEmbeddingModel(mockClient);
            List<String> inputs = IntStream.range(0, 100).mapToObj(String::valueOf).toList();
            List<EmbedTextResponse> embedTextResponses = model.embedAll(inputs);
            Assertions.assertEquals(embedTextResponses.size(), 2);
        }
    }

    private EmbedTextResponse fromVectors(int numEmbeddings) {
        List<List<Float>> embeddings = IntStream.range(0, numEmbeddings).mapToObj(i -> new ArrayList<Float>())
                .collect(toList());
        return new EmbedTextResponse.Builder()
                .embedTextResult(EmbedTextResult.builder()
                        .embeddings(embeddings)
                        .build()
                )
                .build();
    }

    private EmbeddingModel testEmbeddingModel(GenerativeAiInference client) {
        return new EmbeddingModelImpl(client, OnDemandServingMode.builder().modelId("").build(), EmbedTextDetails.Truncate.None, "");
    }
}
