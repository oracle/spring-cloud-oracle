/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle.autoconfigure;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.RegionProvider;
import com.oracle.bmc.generativeai.GenerativeAi;
import com.oracle.bmc.generativeai.GenerativeAiClient;
import com.oracle.bmc.generativeai.model.Model;
import com.oracle.bmc.generativeai.model.ModelCapability;
import com.oracle.bmc.generativeai.model.ModelSummary;
import com.oracle.bmc.generativeai.model.SortOrder;
import com.oracle.bmc.generativeai.requests.ListModelsRequest;
import com.oracle.bmc.generativeai.responses.ListModelsResponse;
import com.oracle.spring.ai.oracle.OracleGenAiChatModel;
import com.oracle.spring.ai.oracle.OracleGenAiChatOptions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Looks up current OCI chat models and verifies chat completions work for the latest model from each provider.
 * Uses OCI config file auth. Specify at least this environment variable to run the test:
 * OCI_COMPARTMENT_ID
 */
class GenAIChatCompletionLiveTest {

    private static final String COMPARTMENT_ID_PROPERTY = "spring.ai.oracle.chat.options.compartment-id";

    private static final String MODEL_PROPERTY = "spring.ai.oracle.chat.options.model";

    private static final String CONFIG_FILE_PROPERTY = "spring.ai.oracle.auth.config-file";

    private static final String PROFILE_PROPERTY = "spring.ai.oracle.auth.profile";

    private static final String REGION_PROPERTY = "spring.ai.oracle.auth.region";

    private static final String COMPARTMENT_ID_ENV = "OCI_COMPARTMENT_ID";

    private static final String MODEL_ENV = "OCI_GENAI_MODEL";

    private static final String CONFIG_FILE_ENV = "OCI_CONFIG_FILE";

    private static final String PROFILE_ENV = "OCI_PROFILE";

    private static final String REGION_ENV = "OCI_REGION";

    @TestFactory
    Stream<DynamicTest> callsOciGenerativeAiWithConfigFileAuthentication() throws Exception {
        String compartmentId = setting(COMPARTMENT_ID_ENV);
        assumeTrue(hasText(compartmentId), "Set " + COMPARTMENT_ID_ENV
                + " to run the live OCI Generative AI test.");

        OracleGenAiAuthenticationProperties authProperties = authenticationProperties();
        BasicAuthenticationDetailsProvider authenticationDetailsProvider =
                authProperties.createBasicAuthenticationDetailsProvider();
        List<ChatModelCandidate> models = loadSelectedModels(authenticationDetailsProvider, authProperties, compartmentId);
        assumeTrue(!models.isEmpty(), "OCI did not return any active chat models for the configured compartment.");

        return models.stream()
                .map(model -> dynamicTest(model.provider() + " latest chat model " + model.modelId()
                        + " uses " + model.apiFormat(),
                        () -> callOciGenerativeAiWithConfigFileAuthentication(
                                compartmentId, model.modelId(), model.apiFormat())));
    }

    private static void callOciGenerativeAiWithConfigFileAuthentication(String compartmentId, String model,
            OracleGenAiChatOptions.ApiFormat apiFormat) {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(OracleGenAiChatAutoConfiguration.class))
                .withPropertyValues(propertyValues(compartmentId, model, apiFormat))
                .run(context -> {
                    assertThat(context).hasSingleBean(ChatModel.class);
                    assertThat(context).hasSingleBean(OracleGenAiChatModel.class);

                    ChatResponse response = context.getBean(ChatModel.class)
                            .call(new Prompt("Reply with one short sentence containing the word oracle."));

                    assertThat(response.getResult().getOutput().getText()).isNotBlank();
                    assertThat(response.getMetadata().getModel()).isNotBlank();
                });
    }

    private static List<ChatModelCandidate> loadSelectedModels(
            BasicAuthenticationDetailsProvider authenticationDetailsProvider,
            OracleGenAiAuthenticationProperties authProperties, String compartmentId) throws Exception {
        String configuredModel = setting(MODEL_ENV);
        Map<String, ChatModelCandidate> latestProviderModels = new LinkedHashMap<>();
        try (GenerativeAi client = GenerativeAiClient.builder().build(authenticationDetailsProvider)) {
            configureRegion(client, authenticationDetailsProvider, authProperties);

            String page = null;
            do {
                ListModelsResponse response = client.listModels(ListModelsRequest.builder()
                        .compartmentId(compartmentId)
                        .capability(ModelCapability.Chat)
                        .lifecycleState(Model.LifecycleState.Active)
                        .sortBy(ListModelsRequest.SortBy.TimeCreated)
                        .sortOrder(SortOrder.Desc)
                        .page(page)
                        .build());
                if (response.getModelCollection() != null && response.getModelCollection().getItems() != null) {
                    for (ModelSummary model : response.getModelCollection().getItems()) {
                        ChatModelCandidate candidate = chatModelCandidate(model);
                        if (candidate == null) {
                            continue;
                        }
                        if (hasText(configuredModel) && candidate.modelId().equals(configuredModel)) {
                            return List.of(candidate);
                        }
                        latestProviderModels.putIfAbsent(candidate.provider(), candidate);
                    }
                }
                page = response.getOpcNextPage();
            }
            while (hasText(page));
        }
        if (hasText(configuredModel)) {
            assumeTrue(false, "Configured OCI Generative AI model " + configuredModel
                    + " was not returned by the live model list call.");
        }
        return List.copyOf(latestProviderModels.values());
    }

    private static ChatModelCandidate chatModelCandidate(ModelSummary model) {
        String modelId = inferenceModelId(model);
        if (!supportsChat(model) || !isAvailableOnDemand(model) || !isTextChatModel(modelId)) {
            return null;
        }
        String provider = modelProvider(modelId, model);
        return new ChatModelCandidate(provider, modelId, apiFormat(modelId, model));
    }

    private static boolean supportsChat(ModelSummary model) {
        return model.getCapabilities() != null && model.getCapabilities().contains(ModelCapability.Chat);
    }

    private static boolean isAvailableOnDemand(ModelSummary model) {
        return model.getTimeDeprecated() == null && model.getTimeOnDemandRetired() == null;
    }

    private static String inferenceModelId(ModelSummary model) {
        if (hasText(model.getDisplayName())) {
            return model.getDisplayName();
        }
        return model.getId();
    }

    private static String modelProvider(String modelId, ModelSummary model) {
        if (hasText(model.getVendor())) {
            return model.getVendor().trim().toLowerCase(Locale.ROOT);
        }
        int separator = modelId.indexOf('.');
        if (separator > 0) {
            return modelId.substring(0, separator).toLowerCase(Locale.ROOT);
        }
        return modelId.toLowerCase(Locale.ROOT);
    }

    private static OracleGenAiChatOptions.ApiFormat apiFormat(String modelId, ModelSummary model) {
        if ("cohere".equalsIgnoreCase(model.getVendor()) || modelId.startsWith("cohere.")) {
            if (modelId.startsWith("cohere.command-a")) {
                return OracleGenAiChatOptions.ApiFormat.COHERE_V2;
            }
            return OracleGenAiChatOptions.ApiFormat.COHERE;
        }
        return OracleGenAiChatOptions.ApiFormat.GENERIC;
    }

    private static boolean isTextChatModel(String modelId) {
        String normalizedModelId = modelId.toLowerCase(Locale.ROOT);
        return !normalizedModelId.contains("audio") && !normalizedModelId.contains("tts")
                && !normalizedModelId.contains("voice");
    }

    private static String[] propertyValues(String compartmentId, String model,
            OracleGenAiChatOptions.ApiFormat apiFormat) {
        List<String> properties = new ArrayList<>();
        properties.add("spring.ai.model.chat=oracle");
        properties.add("spring.ai.oracle.auth.type=FILE");
        properties.add(COMPARTMENT_ID_PROPERTY + "=" + compartmentId);
        properties.add(MODEL_PROPERTY + "=" + model);
        properties.add("spring.ai.oracle.chat.options.api-format=" + apiFormat.name());
        properties.add("spring.ai.oracle.chat.options.temperature=0");
        properties.add("spring.ai.oracle.chat.options.max-tokens=128");
        addIfPresent(properties, CONFIG_FILE_PROPERTY, setting(CONFIG_FILE_ENV));
        addIfPresent(properties, PROFILE_PROPERTY, setting(PROFILE_ENV));
        addIfPresent(properties, REGION_PROPERTY, setting(REGION_ENV));
        return properties.toArray(String[]::new);
    }

    private static OracleGenAiAuthenticationProperties authenticationProperties() {
        OracleGenAiAuthenticationProperties properties = new OracleGenAiAuthenticationProperties();
        properties.setType(OracleGenAiAuthenticationProperties.Type.FILE);
        properties.setConfigFile(setting(CONFIG_FILE_ENV));
        properties.setProfile(setting(PROFILE_ENV));
        properties.setRegion(setting(REGION_ENV));
        return properties;
    }

    private static void configureRegion(GenerativeAi client,
            BasicAuthenticationDetailsProvider authenticationDetailsProvider,
            OracleGenAiAuthenticationProperties authProperties) {
        if (hasText(authProperties.getRegion())) {
            client.setRegion(authProperties.getRegion());
        }
        else if (authenticationDetailsProvider instanceof RegionProvider regionProvider
                && regionProvider.getRegion() != null) {
            client.setRegion(regionProvider.getRegion());
        }
    }

    private static String setting(String environmentVariable) {
        return System.getenv(environmentVariable);
    }

    private static void addIfPresent(List<String> properties, String name, String value) {
        if (hasText(value)) {
            properties.add(name + "=" + value);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private record ChatModelCandidate(String provider, String modelId, OracleGenAiChatOptions.ApiFormat apiFormat) {
    }
}
