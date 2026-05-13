/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle.autoconfigure;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.generativeai.model.ModelCapability;
import com.oracle.bmc.generativeai.model.ModelSummary;
import com.oracle.spring.ai.oracle.OracleGenAiEmbeddingModel;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Looks up current OCI embedding models and verifies one embedding call works for each selected provider.
 * Uses OCI config file auth. Specify at least this environment variable to run the test:
 * OCI_COMPARTMENT_ID
 */
class EmbeddingLiveTest {

    private static final String COMPARTMENT_ID_PROPERTY = PropertyNames.EMBEDDING_CONFIG_PREFIX + ".compartment";

    private static final String MODEL_PROPERTY = PropertyNames.EMBEDDING_CONFIG_PREFIX + ".model";

    private static final String MODEL_ENV = "OCI_GENAI_EMBEDDING_MODEL";

    @TestFactory
    Stream<DynamicTest> callsOciGenerativeAiEmbeddingWithConfigFileAuthentication() throws Exception {
        String compartmentId = TestSupport.requiredCompartmentId(
                "the live OCI Generative AI embedding test");

        AuthenticationProperties authProperties = TestSupport.authenticationProperties();
        BasicAuthenticationDetailsProvider authenticationDetailsProvider =
                AuthenticationProviderFactory.create(authProperties);
        List<EmbeddingModelCandidate> models = TestSupport.loadSelectedModels(authenticationDetailsProvider,
                authProperties, compartmentId, ModelCapability.TextEmbeddings, MODEL_ENV,
                EmbeddingLiveTest::embeddingModelCandidate, "embedding");
        assumeTrue(!models.isEmpty(), "OCI did not return any active embedding models for the configured compartment.");

        return models.stream()
                .map(model -> dynamicTest(model.provider() + " latest embedding model " + model.modelId(),
                        () -> callOciGenerativeAiEmbeddingWithConfigFileAuthentication(compartmentId, model.modelId())));
    }

    private static void callOciGenerativeAiEmbeddingWithConfigFileAuthentication(String compartmentId, String model) {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(GenAiAutoConfiguration.class))
                .withPropertyValues(propertyValues(compartmentId, model))
                .run(context -> {
                    assertThat(context).hasSingleBean(EmbeddingModel.class);
                    assertThat(context).hasSingleBean(OracleGenAiEmbeddingModel.class);

                    float[] embedding = context.getBean(EmbeddingModel.class).embed("Oracle AI Database");

                    assertThat(embedding).isNotEmpty();
                });
    }

    private static EmbeddingModelCandidate embeddingModelCandidate(ModelSummary model) {
        String modelId = TestSupport.inferenceModelId(model);
        if (!TestSupport.supportsCapability(model, ModelCapability.TextEmbeddings)
                || !TestSupport.isAvailableOnDemand(model) || !isTextEmbeddingModel(modelId)) {
            return null;
        }
        return new EmbeddingModelCandidate(TestSupport.modelProvider(modelId, model), modelId);
    }

    private static boolean isTextEmbeddingModel(String modelId) {
        String normalizedModelId = modelId.toLowerCase(Locale.ROOT);
        return !normalizedModelId.contains("image") && !normalizedModelId.contains("multimodal");
    }

    private static String[] propertyValues(String compartmentId, String model) {
        List<String> properties = new ArrayList<>();
        properties.add(PropertyNames.EMBEDDING_MODEL_PROPERTY + "="
                + PropertyNames.MODEL_VALUE);
        properties.add(COMPARTMENT_ID_PROPERTY + "=" + compartmentId);
        properties.add(MODEL_PROPERTY + "=" + model);
        TestSupport.addAuthenticationProperties(properties);
        return properties.toArray(String[]::new);
    }

    private record EmbeddingModelCandidate(String provider, String modelId)
            implements TestSupport.ModelCandidate {
    }
}
