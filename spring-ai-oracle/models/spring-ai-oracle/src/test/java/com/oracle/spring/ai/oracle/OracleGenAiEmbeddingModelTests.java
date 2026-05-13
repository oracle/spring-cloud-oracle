/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle;

import java.util.List;

import com.oracle.bmc.generativeaiinference.model.EmbedTextDetails;
import com.oracle.bmc.generativeaiinference.model.EmbedTextResult;
import com.oracle.bmc.generativeaiinference.model.OnDemandServingMode;
import com.oracle.bmc.generativeaiinference.model.Usage;
import com.oracle.bmc.generativeaiinference.requests.EmbedTextRequest;
import com.oracle.bmc.generativeaiinference.responses.EmbedTextResponse;
import com.oracle.spring.ai.oracle.test.NoOpGenerativeAiInference;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OracleGenAiEmbeddingModelTests {

    @Test
    void createsOnDemandRequest() {
        OracleGenAiEmbeddingOptions options = defaultOptions();
        options.setDimensions(512);
        options.setTruncate(OracleGenAiEmbeddingOptions.Truncate.END);

        EmbedTextRequest request = new OracleGenAiEmbeddingModel(new NoOpGenerativeAiInference(), options)
                .toEmbedTextRequest(List.of("first", "second"), options);

        EmbedTextDetails details = request.getEmbedTextDetails();
        assertThat(details.getInputs()).containsExactly("first", "second");
        assertThat(details.getCompartmentId()).isEqualTo("compartment");
        assertThat(details.getServingMode()).isInstanceOf(OnDemandServingMode.class);
        assertThat(((OnDemandServingMode) details.getServingMode()).getModelId()).isEqualTo("model");
        assertThat(details.getOutputDimensions()).isEqualTo(512);
        assertThat(details.getTruncate()).isEqualTo(EmbedTextDetails.Truncate.End);
    }

    @Test
    void createsDedicatedRequest() {
        OracleGenAiEmbeddingOptions options = defaultOptions();
        options.setServingMode(OracleGenAiEmbeddingOptions.ServingMode.DEDICATED);
        options.setEndpointId("endpoint");

        EmbedTextRequest request = new OracleGenAiEmbeddingModel(new NoOpGenerativeAiInference(), options)
                .toEmbedTextRequest(List.of("text"), options);

        assertThat(request.getEmbedTextDetails().getServingMode())
                .isInstanceOf(com.oracle.bmc.generativeaiinference.model.DedicatedServingMode.class);
    }

    @Test
    void validatesOnDemandModelId() {
        OracleGenAiEmbeddingOptions options = defaultOptions();
        options.setModel(null);
        OracleGenAiEmbeddingModel model = new OracleGenAiEmbeddingModel(new NoOpGenerativeAiInference(), options);

        assertThatThrownBy(() -> model.toEmbedTextRequest(List.of("text"), options))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("options.model");
    }

    @Test
    void validatesDedicatedEndpointId() {
        OracleGenAiEmbeddingOptions options = defaultOptions();
        options.setServingMode(OracleGenAiEmbeddingOptions.ServingMode.DEDICATED);
        options.setEndpointId(null);
        OracleGenAiEmbeddingModel model = new OracleGenAiEmbeddingModel(new NoOpGenerativeAiInference(), options);

        assertThatThrownBy(() -> model.toEmbedTextRequest(List.of("text"), options))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("options.endpointId");
    }

    @Test
    void validatesCompartmentId() {
        OracleGenAiEmbeddingOptions options = defaultOptions();
        options.setCompartmentId(null);
        OracleGenAiEmbeddingModel model = new OracleGenAiEmbeddingModel(new NoOpGenerativeAiInference(), options);

        assertThatThrownBy(() -> model.toEmbedTextRequest(List.of("text"), options))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("compartmentId");
    }

    @Test
    void runtimeOptionsOverrideDefaults() {
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(responseWithEmbeddings(
                List.of(List.of(0.1f))));
        OracleGenAiEmbeddingOptions runtimeOptions = OracleGenAiEmbeddingOptions.builder()
                .compartmentId("runtime-compartment")
                .model("runtime-model")
                .dimensions(128)
                .truncate(OracleGenAiEmbeddingOptions.Truncate.START)
                .build();

        new OracleGenAiEmbeddingModel(client, defaultOptions())
                .call(new EmbeddingRequest(List.of("text"), runtimeOptions));

        EmbedTextDetails details = client.request.getEmbedTextDetails();
        assertThat(details.getCompartmentId()).isEqualTo("runtime-compartment");
        assertThat(((OnDemandServingMode) details.getServingMode()).getModelId()).isEqualTo("runtime-model");
        assertThat(details.getOutputDimensions()).isEqualTo(128);
        assertThat(details.getTruncate()).isEqualTo(EmbedTextDetails.Truncate.Start);
    }

    @Test
    void convertsEmbeddingResponse() {
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(responseWithEmbeddings(
                List.of(List.of(0.1f, 0.2f), List.of(0.3f, 0.4f))));

        EmbeddingResponse response = new OracleGenAiEmbeddingModel(client, defaultOptions())
                .call(new EmbeddingRequest(List.of("first", "second"), null));

        assertThat(response.getResults()).hasSize(2);
        assertThat(response.getResults().get(0).getOutput()).containsExactly(0.1f, 0.2f);
        assertThat(response.getResults().get(0).getIndex()).isZero();
        assertThat(response.getResults().get(1).getOutput()).containsExactly(0.3f, 0.4f);
        assertThat(response.getMetadata().getModel()).isEqualTo("response-model");
        assertThat(response.getMetadata().getUsage().getPromptTokens()).isEqualTo(7);
        assertThat(response.getMetadata().getUsage().getTotalTokens()).isEqualTo(7);
        assertThat((String) response.getMetadata().get("id")).isEqualTo("embedding-id");
        assertThat((String) response.getMetadata().get("modelVersion")).isEqualTo("v1");
        assertThat((String) response.getMetadata().get("opcRequestId")).isEqualTo("request-id");
        assertThat((String) response.getMetadata().get("modelDeprecationInfo")).isEqualTo("deprecated");
    }

    @Test
    void rejectsMissingEmbeddingResult() {
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(EmbedTextResponse.builder().build());

        assertThatThrownBy(() -> new OracleGenAiEmbeddingModel(client, defaultOptions())
                .call(new EmbeddingRequest(List.of("text"), null)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("embedding result");
    }

    @Test
    void rejectsMissingEmbeddings() {
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(EmbedTextResponse.builder()
                .embedTextResult(EmbedTextResult.builder().build())
                .build());

        assertThatThrownBy(() -> new OracleGenAiEmbeddingModel(client, defaultOptions())
                .call(new EmbeddingRequest(List.of("text"), null)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("embeddings");
    }

    @Test
    void reportsNullVectorPosition() {
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(responseWithEmbeddings(
                List.of(java.util.Arrays.asList(0.1f, null))));

        assertThatThrownBy(() -> new OracleGenAiEmbeddingModel(client, defaultOptions())
                .call(new EmbeddingRequest(List.of("text"), null)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("embedding 0, vector position 1");
    }

    private static OracleGenAiEmbeddingOptions defaultOptions() {
        return OracleGenAiEmbeddingOptions.builder()
                .compartmentId("compartment")
                .model("model")
                .build();
    }

    private static EmbedTextResponse responseWithEmbeddings(List<List<Float>> embeddings) {
        return EmbedTextResponse.builder()
                .opcRequestId("request-id")
                .modelDeprecationInfo("deprecated")
                .embedTextResult(EmbedTextResult.builder()
                        .id("embedding-id")
                        .modelId("response-model")
                        .modelVersion("v1")
                        .usage(Usage.builder()
                                .promptTokens(7)
                                .completionTokens(0)
                                .totalTokens(7)
                                .build())
                        .embeddings(embeddings)
                        .build())
                .build();
    }

    private static final class CapturingGenerativeAiInference extends NoOpGenerativeAiInference {

        private final EmbedTextResponse response;

        private EmbedTextRequest request;

        private CapturingGenerativeAiInference(EmbedTextResponse response) {
            this.response = response;
        }

        @Override
        public EmbedTextResponse embedText(EmbedTextRequest request) {
            this.request = request;
            return response;
        }
    }
}
