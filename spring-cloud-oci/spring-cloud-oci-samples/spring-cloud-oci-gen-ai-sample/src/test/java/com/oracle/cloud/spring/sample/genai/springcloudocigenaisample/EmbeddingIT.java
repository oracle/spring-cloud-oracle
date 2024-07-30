/*
 ** Copyright (c) 2024, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.sample.genai.springcloudocigenaisample;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.oracle.bmc.generativeaiinference.responses.EmbedTextResponse;
import com.oracle.cloud.spring.genai.EmbeddingModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OCI_COMPARTMENT_ID", matches = ".+")
@ActiveProfiles("embedding")
public class EmbeddingIT {
    @Autowired
    EmbeddingModel embeddingModel;

    @Test
    public void embed() {
        EmbedTextResponse response = embeddingModel.embed("The USA has 50 states.");
        List<List<Float>> embeddings = embeddingModel.fromResponse(response);
        Assertions.assertEquals(1, embeddings.size());
    }

    @Test
    public void embedAll() {
        List<EmbedTextResponse> responses = embeddingModel.embedAll(IntStream.range(0, 3).mapToObj(i -> String.format("embedding %d", i)).collect(Collectors.toList()));
        List<List<Float>> embeddings = embeddingModel.fromResponses(responses);
        Assertions.assertEquals(3, embeddings.size());
    }
}
