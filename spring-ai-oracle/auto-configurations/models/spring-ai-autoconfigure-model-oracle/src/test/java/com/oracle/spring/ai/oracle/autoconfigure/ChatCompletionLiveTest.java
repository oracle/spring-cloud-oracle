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
import com.oracle.spring.ai.oracle.OracleGenAiChatApiFormat;
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
class ChatCompletionLiveTest {

    private static final String COMPARTMENT_ID_PROPERTY = PropertyNames.CHAT_CONFIG_PREFIX + ".compartment-id";

    private static final String MODEL_PROPERTY = PropertyNames.CHAT_CONFIG_PREFIX + ".model";

    private static final String MODEL_ENV = "OCI_GENAI_MODEL";

    @TestFactory
    Stream<DynamicTest> callsOciGenerativeAiWithConfigFileAuthentication() throws Exception {
        String compartmentId = TestSupport.requiredCompartmentId("the live OCI Generative AI test");

        AuthenticationProperties authProperties = TestSupport.authenticationProperties();
        BasicAuthenticationDetailsProvider authenticationDetailsProvider =
                AuthenticationProviderFactory.create(authProperties);
        List<ChatModelCandidate> models = TestSupport.loadSelectedModels(authenticationDetailsProvider,
                authProperties, compartmentId, ModelCapability.Chat, MODEL_ENV,
                ChatCompletionLiveTest::chatModelCandidate, "chat");
        assumeTrue(!models.isEmpty(), "OCI did not return any active chat models for the configured compartment.");

        return models.stream()
                .map(model -> dynamicTest(model.provider() + " latest chat model " + model.modelId()
                        + " uses " + model.apiFormat(),
                        () -> callOciGenerativeAiWithConfigFileAuthentication(
                                compartmentId, model.modelId(), model.apiFormat())));
    }

    private static void callOciGenerativeAiWithConfigFileAuthentication(String compartmentId, String model,
            OracleGenAiChatApiFormat apiFormat) {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(GenAiAutoConfiguration.class))
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

    private static ChatModelCandidate chatModelCandidate(ModelSummary model) {
        String modelId = TestSupport.inferenceModelId(model);
        if (!TestSupport.supportsCapability(model, ModelCapability.Chat)
                || !TestSupport.isAvailableOnDemand(model) || !isTextChatModel(modelId)) {
            return null;
        }
        String provider = TestSupport.modelProvider(modelId, model);
        return new ChatModelCandidate(provider, modelId, apiFormat(modelId));
    }

    private static OracleGenAiChatApiFormat apiFormat(String modelId) {
        return OracleGenAiChatOptions.inferApiFormat(modelId);
    }

    private static boolean isTextChatModel(String modelId) {
        String normalizedModelId = modelId.toLowerCase(Locale.ROOT);
        return !normalizedModelId.contains("audio") && !normalizedModelId.contains("tts")
                && !normalizedModelId.contains("voice");
    }

    private static String[] propertyValues(String compartmentId, String model,
            OracleGenAiChatApiFormat apiFormat) {
        List<String> properties = new ArrayList<>();
        properties.add(PropertyNames.CHAT_MODEL_PROPERTY + "=" + PropertyNames.MODEL_VALUE);
        properties.add(COMPARTMENT_ID_PROPERTY + "=" + compartmentId);
        properties.add(MODEL_PROPERTY + "=" + model);
        properties.add(PropertyNames.CHAT_CONFIG_PREFIX + ".api-format=" + apiFormat.name());
        properties.add(PropertyNames.CHAT_CONFIG_PREFIX + ".temperature=0");
        properties.add(PropertyNames.CHAT_CONFIG_PREFIX + ".max-tokens=128");
        TestSupport.addAuthenticationProperties(properties);
        return properties.toArray(String[]::new);
    }

    private record ChatModelCandidate(String provider, String modelId, OracleGenAiChatApiFormat apiFormat)
            implements TestSupport.ModelCandidate {
    }
}
