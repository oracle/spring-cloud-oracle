/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle.autoconfigure;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.generativeai.model.ModelCapability;
import com.oracle.bmc.generativeai.model.ModelSummary;
import com.oracle.spring.ai.oracle.api.GenAiApiFormat;
import com.oracle.spring.ai.oracle.OracleGenAiChatModel;
import com.oracle.spring.ai.oracle.OracleGenAiChatOptions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.oracle.spring.ai.oracle.api.GenAiApiFormat.infer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
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

    private static final String TOOL_RESULT = "oracle-live-tool-result";

    private static final String TOOL_NAME = "oracle_live_test_marker";

    private static final String TOOL_INPUT_SCHEMA = """
            {"type":"object","properties":{}}
            """;

    private static final Duration LIVE_TEST_TIMEOUT = Duration.ofMinutes(2);

    @TestFactory
    Stream<DynamicTest> callsOciGenerativeAiWithConfigFileAuthentication() throws Exception {
        String compartmentId = TestSupport.requiredCompartmentId("the live OCI Generative AI test");

        AuthenticationProperties authProperties = TestSupport.authenticationProperties();
        BasicAuthenticationDetailsProvider authenticationDetailsProvider =
                AuthenticationProviderFactory.create(authProperties);

        return Stream.of(
                new ChatScenario("chat", ChatCompletionLiveTest::chatModelCandidate,
                        ChatCompletionLiveTest::callOciGenerativeAiWithConfigFileAuthentication),
                new ChatScenario("streaming chat", ChatCompletionLiveTest::chatModelCandidate,
                        ChatCompletionLiveTest::streamOciGenerativeAiWithConfigFileAuthentication),
                new ChatScenario("tool-capable chat", ChatCompletionLiveTest::toolChatModelCandidate,
                        ChatCompletionLiveTest::callOciGenerativeAiWithToolCalling))
                .flatMap(scenario -> dynamicTests(authenticationDetailsProvider, authProperties,
                        compartmentId, scenario));
    }

    private static void callOciGenerativeAiWithConfigFileAuthentication(String compartmentId, String model,
            GenAiApiFormat apiFormat) {
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

    private static void streamOciGenerativeAiWithConfigFileAuthentication(String compartmentId, String model,
            GenAiApiFormat apiFormat) {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(GenAiAutoConfiguration.class))
                .withPropertyValues(propertyValues(compartmentId, model, apiFormat))
                .run(context -> {
                    assertThat(context).hasSingleBean(ChatModel.class);
                    assertThat(context).hasSingleBean(OracleGenAiChatModel.class);

                    List<ChatResponse> responses = context.getBean(ChatModel.class)
                            .stream(new Prompt("Reply with one short sentence containing the word oracle."))
                            .collectList()
                            .block(LIVE_TEST_TIMEOUT);
                    assertThat(responses).isNotEmpty();
                    assertThat(responses)
                            .extracting(response -> response.getResult().getOutput().getText())
                            .anySatisfy(text -> assertThat(text).isNotBlank());
                    assertThat(responses)
                            .extracting(response -> response.getMetadata().getModel())
                            .anySatisfy(modelId -> assertThat(modelId).isNotBlank());
                });
    }

    private static List<ChatModelCandidate> loadSelectedChatModels(
            BasicAuthenticationDetailsProvider authenticationDetailsProvider,
            AuthenticationProperties authProperties, String compartmentId,
            Function<ModelSummary, ChatModelCandidate> candidateFactory, String modelDescription) {
        return assertTimeoutPreemptively(LIVE_TEST_TIMEOUT,
                () -> TestSupport.loadSelectedModels(authenticationDetailsProvider, authProperties, compartmentId,
                        ModelCapability.Chat, MODEL_ENV, candidateFactory, modelDescription));
    }

    private static Stream<DynamicTest> dynamicTests(
            BasicAuthenticationDetailsProvider authenticationDetailsProvider,
            AuthenticationProperties authProperties, String compartmentId, ChatScenario scenario) {
        List<ChatModelCandidate> models = loadSelectedChatModels(authenticationDetailsProvider,
                authProperties, compartmentId, scenario.candidateFactory(), scenario.modelDescription());
        if (models.isEmpty()) {
            return Stream.of(dynamicTest("OCI returns active " + scenario.modelDescription() + " models",
                    () -> assumeTrue(false, "OCI did not return any active " + scenario.modelDescription()
                            + " models for the configured compartment.")));
        }
        return models.stream()
                .map(model -> dynamicTest(model.provider() + " latest " + scenario.modelDescription() + " model "
                        + model.modelId() + " uses " + model.apiFormat(),
                        () -> assertTimeoutPreemptively(LIVE_TEST_TIMEOUT,
                                () -> scenario.assertion()
                                        .run(compartmentId, model.modelId(), model.apiFormat()))));
    }

    private static void callOciGenerativeAiWithToolCalling(String compartmentId, String model,
            GenAiApiFormat apiFormat) {
        AtomicBoolean toolInvoked = new AtomicBoolean();
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(GenAiAutoConfiguration.class))
                .withPropertyValues(propertyValues(compartmentId, model, apiFormat))
                .run(context -> {
                    assertThat(context).hasSingleBean(ChatModel.class);
                    assertThat(context).hasSingleBean(OracleGenAiChatModel.class);

                    ChatResponse response = context.getBean(ChatModel.class)
                            .call(new Prompt("Call the " + TOOL_NAME + " tool. Do not answer without using it.",
                                    OracleGenAiChatOptions.builder()
                                            .toolCallbacks(List.of(toolInvocationMarker(toolInvoked)))
                                            .internalToolExecutionEnabled(true)
                                            .build()));

                    assertThat(toolInvoked).isTrue();
                    assertThat(response.getResult().getOutput().getText()).isEqualTo(TOOL_RESULT);
                });
    }

    private static ChatModelCandidate chatModelCandidate(ModelSummary model) {
        String modelId = TestSupport.inferenceModelId(model);
        if (!TestSupport.supportsCapability(model, ModelCapability.Chat)
                || !TestSupport.isAvailableOnDemand(model) || !isTextChatModel(modelId)) {
            return null;
        }
        String provider = TestSupport.modelProvider(modelId, model);
        return new ChatModelCandidate(provider, modelId, infer(modelId));
    }

    private static ChatModelCandidate toolChatModelCandidate(ModelSummary model) {
        ChatModelCandidate candidate = chatModelCandidate(model);
        if (candidate == null || !supportsToolCalling(candidate.modelId(), candidate.apiFormat())) {
            return null;
        }
        return candidate;
    }

    private static boolean supportsToolCalling(String modelId, GenAiApiFormat apiFormat) {
        String normalizedModelId = modelId.toLowerCase(Locale.ROOT);
        return (apiFormat == GenAiApiFormat.GENERIC || apiFormat == GenAiApiFormat.COHERE_V2)
                && !normalizedModelId.contains("vision");
    }

    private static boolean isTextChatModel(String modelId) {
        String normalizedModelId = modelId.toLowerCase(Locale.ROOT);
        return !normalizedModelId.contains("audio") && !normalizedModelId.contains("tts")
                && !normalizedModelId.contains("voice");
    }

    private static String[] propertyValues(String compartmentId, String model,
            GenAiApiFormat apiFormat) {
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

    private static ToolCallback toolInvocationMarker(AtomicBoolean toolInvoked) {
        return new ToolCallback() {
            @Override
            public ToolDefinition getToolDefinition() {
                return ToolDefinition.builder()
                        .name(TOOL_NAME)
                        .description("Returns the live test marker value.")
                        .inputSchema(TOOL_INPUT_SCHEMA)
                        .build();
            }

            @Override
            public ToolMetadata getToolMetadata() {
                return ToolMetadata.builder().returnDirect(true).build();
            }

            @Override
            public String call(String toolInput) {
                toolInvoked.set(true);
                return TOOL_RESULT;
            }
        };
    }

    private record ChatModelCandidate(String provider, String modelId, GenAiApiFormat apiFormat)
            implements TestSupport.ModelCandidate {
    }

    private record ChatScenario(String modelDescription,
            Function<ModelSummary, ChatModelCandidate> candidateFactory, ChatAssertion assertion) {
    }

    @FunctionalInterface
    private interface ChatAssertion {
        void run(String compartmentId, String model, GenAiApiFormat apiFormat);
    }
}
