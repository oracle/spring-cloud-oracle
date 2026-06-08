/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.oracle.bmc.generativeaiinference.model.BaseChatRequest;
import com.oracle.bmc.generativeaiinference.model.BaseChatResponse;
import com.oracle.bmc.generativeaiinference.model.ChatContent;
import com.oracle.bmc.generativeaiinference.model.ChatResult;
import com.oracle.bmc.generativeaiinference.model.CohereAssistantMessageV2;
import com.oracle.bmc.generativeaiinference.model.CohereChatBotMessage;
import com.oracle.bmc.generativeaiinference.model.CohereChatRequest;
import com.oracle.bmc.generativeaiinference.model.CohereChatRequestV2;
import com.oracle.bmc.generativeaiinference.model.CohereContentV2;
import com.oracle.bmc.generativeaiinference.model.CohereMessage;
import com.oracle.bmc.generativeaiinference.model.CohereMessageV2;
import com.oracle.bmc.generativeaiinference.model.CohereToolCallV2;
import com.oracle.bmc.generativeaiinference.model.CohereToolMessageV2;
import com.oracle.bmc.generativeaiinference.model.CohereToolV2;
import com.oracle.bmc.generativeaiinference.model.CohereSystemMessageV2;
import com.oracle.bmc.generativeaiinference.model.CohereTextContentV2;
import com.oracle.bmc.generativeaiinference.model.CohereUserMessage;
import com.oracle.bmc.generativeaiinference.model.CohereUserMessageV2;
import com.oracle.bmc.generativeaiinference.model.FunctionCall;
import com.oracle.bmc.generativeaiinference.model.FunctionDefinition;
import com.oracle.bmc.generativeaiinference.model.GenericChatRequest;
import com.oracle.bmc.generativeaiinference.model.GenericChatResponse;
import com.oracle.bmc.generativeaiinference.model.TextContent;
import com.oracle.bmc.generativeaiinference.model.ToolMessage;
import com.oracle.bmc.generativeaiinference.model.Usage;
import com.oracle.bmc.generativeaiinference.requests.ChatRequest;
import com.oracle.spring.ai.oracle.api.GenAiApiFormat;
import com.oracle.spring.ai.oracle.api.OracleGenAiServingMode;
import com.oracle.spring.ai.oracle.converter.ChatStreamResponseConverter;
import com.oracle.spring.ai.oracle.test.NoOpGenerativeAiInference;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionEligibilityChecker;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.core.retry.RetryTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OracleGenAiChatModelTests {

    private static final NoOpGenerativeAiInference CLIENT = new NoOpGenerativeAiInference();

    @Test
    void createsGenericRequest() {
        BaseChatRequest request = model(CLIENT, defaultOptions())
                .toBaseChatRequest(new Prompt(List.of(
                        new SystemMessage("answer briefly"),
                        new UserMessage("hello"))), defaultOptions());

        assertThat(request).isInstanceOf(GenericChatRequest.class);
        GenericChatRequest genericRequest = (GenericChatRequest) request;
        assertThat(genericRequest.getMessages()).hasSize(2);
        assertThat(genericRequest.getMaxTokens()).isEqualTo(20);
        assertThat(genericRequest.getTemperature()).isEqualTo(0.2);
    }

    @Test
    void createsGenericRequestWithChatHistory() {
        BaseChatRequest request = model(CLIENT, defaultOptions())
                .toBaseChatRequest(chatHistoryPrompt(), defaultOptions());

        assertThat(request).isInstanceOf(GenericChatRequest.class);
        GenericChatRequest genericRequest = (GenericChatRequest) request;
        assertThat(genericRequest.getMessages())
                .hasSize(4)
                .satisfiesExactly(
                        message -> {
                            assertThat(message)
                                    .isInstanceOf(com.oracle.bmc.generativeaiinference.model.SystemMessage.class);
                            assertThat(genericMessageText(message)).isEqualTo("answer briefly");
                        },
                        message -> {
                            assertThat(message)
                                    .isInstanceOf(com.oracle.bmc.generativeaiinference.model.UserMessage.class);
                            assertThat(genericMessageText(message)).isEqualTo("hello");
                        },
                        message -> {
                            assertThat(message)
                                    .isInstanceOf(com.oracle.bmc.generativeaiinference.model.AssistantMessage.class);
                            assertThat(genericMessageText(message)).isEqualTo("hi there");
                        },
                        message -> {
                            assertThat(message)
                                    .isInstanceOf(com.oracle.bmc.generativeaiinference.model.UserMessage.class);
                            assertThat(genericMessageText(message)).isEqualTo("what did I say?");
                        });
    }

    @Test
    void createsCohereV2Request() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setApiFormat(GenAiApiFormat.COHERE_V2);

        BaseChatRequest request = model(CLIENT, options)
                .toBaseChatRequest(new Prompt(List.of(new SystemMessage("rules"), new UserMessage("hello"))), options);

        assertThat(request).isInstanceOf(CohereChatRequestV2.class);
        assertThat(((CohereChatRequestV2) request).getMessages()).hasSize(2);
    }

    @Test
    void createsCohereV2RequestWithChatHistory() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setApiFormat(GenAiApiFormat.COHERE_V2);

        BaseChatRequest request = model(CLIENT, options)
                .toBaseChatRequest(chatHistoryPrompt(), options);

        assertThat(request).isInstanceOf(CohereChatRequestV2.class);
        CohereChatRequestV2 cohereRequest = (CohereChatRequestV2) request;
        assertThat(cohereRequest.getMessages())
                .hasSize(4)
                .satisfiesExactly(
                        message -> {
                            assertThat(message).isInstanceOf(CohereSystemMessageV2.class);
                            assertThat(cohereV2MessageText(message)).isEqualTo("answer briefly");
                        },
                        message -> {
                            assertThat(message).isInstanceOf(CohereUserMessageV2.class);
                            assertThat(cohereV2MessageText(message)).isEqualTo("hello");
                        },
                        message -> {
                            assertThat(message).isInstanceOf(CohereAssistantMessageV2.class);
                            assertThat(cohereV2MessageText(message)).isEqualTo("hi there");
                        },
                        message -> {
                            assertThat(message).isInstanceOf(CohereUserMessageV2.class);
                            assertThat(cohereV2MessageText(message)).isEqualTo("what did I say?");
                        });
    }

    @Test
    void infersCohereV2RequestForCommandAModels() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setModel("cohere.command-a-03-2025");

        BaseChatRequest request = model(CLIENT, options)
                .toBaseChatRequest(new Prompt("hello"), options);

        assertThat(request).isInstanceOf(CohereChatRequestV2.class);
    }

    @Test
    void infersLegacyCohereRequestForCommandRModels() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setModel("cohere.command-r-plus-08-2024");

        BaseChatRequest request = model(CLIENT, options)
                .toBaseChatRequest(new Prompt("hello"), options);

        assertThat(request).isInstanceOf(CohereChatRequest.class);
    }

    @Test
    void createsLegacyCohereRequest() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setApiFormat(GenAiApiFormat.COHERE);

        BaseChatRequest request = model(CLIENT, options)
                .toBaseChatRequest(new Prompt(List.of(
                        new SystemMessage("rules"),
                        new UserMessage("first"),
                        new AssistantMessage("second"),
                        new UserMessage("third"))), options);

        assertThat(request).isInstanceOf(CohereChatRequest.class);
        CohereChatRequest cohereRequest = (CohereChatRequest) request;
        assertThat(cohereRequest.getMessage()).isEqualTo("third");
        assertThat(cohereRequest.getPreambleOverride()).isEqualTo("rules");
        assertThat(cohereRequest.getChatHistory()).hasSize(2);
    }

    @Test
    void createsLegacyCohereRequestWithChatHistory() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setApiFormat(GenAiApiFormat.COHERE);

        BaseChatRequest request = model(CLIENT, options)
                .toBaseChatRequest(chatHistoryPrompt(), options);

        assertThat(request).isInstanceOf(CohereChatRequest.class);
        CohereChatRequest cohereRequest = (CohereChatRequest) request;
        assertThat(cohereRequest.getPreambleOverride()).isEqualTo("answer briefly");
        assertThat(cohereRequest.getMessage()).isEqualTo("what did I say?");
        assertThat(cohereRequest.getChatHistory())
                .hasSize(2)
                .satisfiesExactly(
                        message -> {
                            assertThat(message).isInstanceOf(CohereUserMessage.class);
                            assertThat(legacyCohereMessageText(message)).isEqualTo("hello");
                        },
                        message -> {
                            assertThat(message).isInstanceOf(CohereChatBotMessage.class);
                            assertThat(legacyCohereMessageText(message)).isEqualTo("hi there");
                        });
    }

    @Test
    void validatesOnDemandModelId() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setModel(null);
        OracleGenAiChatModel model = model(CLIENT, options);

        assertThatThrownBy(() -> model.call(new Prompt("hello")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("options.model");
    }

    @Test
    void validatesDedicatedEndpointId() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setServingMode(OracleGenAiServingMode.DEDICATED);
        options.setEndpointId(null);
        OracleGenAiChatModel model = model(CLIENT, options);

        assertThatThrownBy(() -> model.call(new Prompt("hello")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("options.endpointId");
    }

    @Test
    void createsDedicatedServingMode() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setServingMode(OracleGenAiServingMode.DEDICATED);
        options.setEndpointId("endpoint");
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(genericTextResponse("hello"));

        model(client, options).call(new Prompt("hello"));
        ChatRequest request = client.requests().get(0);

        assertThat(request.getChatDetails().getServingMode())
                .isInstanceOf(com.oracle.bmc.generativeaiinference.model.DedicatedServingMode.class);
    }

    @Test
    void copiesAndMergesToolCallingOptions() {
        ToolCallback defaultTool = tool("weather", false);
        ToolCallback runtimeTool = tool("time", false);
        OracleGenAiChatOptions defaults = defaultOptions();
        defaults.setToolCallbacks(List.of(defaultTool));
        defaults.setToolContext(Map.of("default", "value"));
        OracleGenAiChatOptions runtime = OracleGenAiChatOptions.builder()
                .toolCallbacks(List.of(runtimeTool))
                .toolContext(Map.of("runtime", "value"))
                .build();

        OracleGenAiChatOptions merged = defaults.merge(runtime);

        assertThat(merged.getToolCallbacks()).containsExactly(runtimeTool);
        assertThat(merged.getToolContext()).containsEntry("default", "value").containsEntry("runtime", "value");
    }

    @Test
    void builderCreatesModelWithDefaults() {
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(genericTextResponse("hello"));

        ChatResponse response = OracleGenAiChatModel.builder()
                .client(client)
                .defaultOptions(defaultOptions())
                .build()
                .call(new Prompt("hello"));

        assertThat(response.getResult().getOutput().getText()).isEqualTo("hello");
    }

    @Test
    void builderUsesExplicitDependencies() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setToolCallbacks(List.of(tool("weather", false)));
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(genericToolCallResponse());
        ToolCallingManager toolCallingManager = ToolCallingManager.builder().build();
        ToolExecutionEligibilityChecker toolExecutionEligibilityChecker = chatResponse -> false;
        RetryTemplate retryTemplate = RetryUtils.DEFAULT_RETRY_TEMPLATE;
        ObservationRegistry observationRegistry = ObservationRegistry.create();

        ChatResponse response = OracleGenAiChatModel.builder()
                .client(client)
                .defaultOptions(options)
                .toolCallingManager(toolCallingManager)
                .toolExecutionEligibilityChecker(toolExecutionEligibilityChecker)
                .retryTemplate(retryTemplate)
                .observationRegistry(observationRegistry)
                .build()
                .call(new Prompt("What is the weather in Seattle?"));

        assertThat(response.getResult().getOutput().getToolCalls()).singleElement();
        assertThat(client.requests()).hasSize(1);
    }

    @Test
    void observesChatCall() {
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(genericTextResponse("hello"));
        RecordingObservationHandler handler = new RecordingObservationHandler(ChatModelObservationContext.class);
        ObservationRegistry observationRegistry = ObservationRegistry.create();
        observationRegistry.observationConfig().observationHandler(handler);

        ChatResponse response = OracleGenAiChatModel.builder()
                .client(client)
                .defaultOptions(defaultOptions())
                .observationRegistry(observationRegistry)
                .build()
                .call(new Prompt("hello"));

        assertThat(response.getResult().getOutput().getText()).isEqualTo("hello");
        assertThat(handler.stoppedContexts())
                .singleElement()
                .isInstanceOfSatisfying(ChatModelObservationContext.class, context -> {
                    assertThat(context.getOperationMetadata().provider()).isEqualTo("oci_genai");
                    assertThat(context.isStreaming()).isFalse();
                    assertThat(context.getRequest().getContents()).isEqualTo("hello");
                    assertThat(context.getResponse()).isSameAs(response);
                });
    }

    @Test
    void observesChatCallErrors() {
        RuntimeException failure = new IllegalStateException("chat failed");
        RecordingObservationHandler handler = new RecordingObservationHandler(ChatModelObservationContext.class);
        ObservationRegistry observationRegistry = ObservationRegistry.create();
        observationRegistry.observationConfig().observationHandler(handler);
        OracleGenAiChatModel model = OracleGenAiChatModel.builder()
                .client(new FailingGenerativeAiInference(failure))
                .defaultOptions(defaultOptions())
                .observationRegistry(observationRegistry)
                .build();

        assertThatThrownBy(() -> model.call(new Prompt("hello"))).isSameAs(failure);

        assertThat(handler.errorContexts())
                .singleElement()
                .isInstanceOfSatisfying(ChatModelObservationContext.class, context ->
                        assertThat(context.getError()).isSameAs(failure));
    }

    @Test
    void createsGenericRequestWithToolsAndToolMessages() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setToolCallbacks(List.of(tool("weather", false)));

        BaseChatRequest request = model(CLIENT, options)
                .toBaseChatRequest(toolConversationPrompt(), options);

        assertThat(request).isInstanceOf(GenericChatRequest.class);
        GenericChatRequest genericRequest = (GenericChatRequest) request;
        assertThat(genericRequest.getTools())
                .singleElement()
                .isInstanceOfSatisfying(FunctionDefinition.class, tool -> {
                    assertThat(tool.getName()).isEqualTo("weather");
                    assertThat(tool.getDescription()).isEqualTo("Weather lookup");
                    assertThat(tool.getParameters()).isInstanceOf(Map.class);
                });
        assertThat(genericRequest.getMessages())
                .hasSize(4)
                .satisfiesExactly(
                        message -> assertThat(message)
                                .isInstanceOf(com.oracle.bmc.generativeaiinference.model.UserMessage.class),
                        message -> assertThat(message)
                                .isInstanceOfSatisfying(com.oracle.bmc.generativeaiinference.model.AssistantMessage.class,
                                        assistant -> {
                                            assertThat(assistant.getToolCalls())
                                                    .singleElement()
                                                    .isInstanceOfSatisfying(FunctionCall.class, toolCall -> {
                                                        assertThat(toolCall.getId()).isEqualTo("call-1");
                                                        assertThat(toolCall.getName()).isEqualTo("weather");
                                                        assertThat(toolCall.getArguments()).isEqualTo("{\"city\":\"Seattle\"}");
                                                    });
                                        }),
                        message -> assertThat(message)
                                .isInstanceOfSatisfying(ToolMessage.class, toolMessage -> {
                                    assertThat(toolMessage.getToolCallId()).isEqualTo("call-1");
                                    assertThat(genericMessageText(toolMessage)).isEqualTo("{\"temp\":55}");
                                }),
                        message -> assertThat(message)
                                .isInstanceOf(com.oracle.bmc.generativeaiinference.model.UserMessage.class));
    }

    @Test
    void createsCohereV2RequestWithToolsAndToolMessages() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setApiFormat(GenAiApiFormat.COHERE_V2);
        options.setToolCallbacks(List.of(tool("weather", false)));

        BaseChatRequest request = model(CLIENT, options)
                .toBaseChatRequest(toolConversationPrompt(), options);

        assertThat(request).isInstanceOf(CohereChatRequestV2.class);
        CohereChatRequestV2 cohereRequest = (CohereChatRequestV2) request;
        assertThat(cohereRequest.getTools())
                .singleElement()
                .isInstanceOfSatisfying(CohereToolV2.class, tool -> {
                    assertThat(tool.getType()).isEqualTo(CohereToolV2.Type.Function);
                    assertThat(tool.getFunction().getName()).isEqualTo("weather");
                });
        assertThat(cohereRequest.getMessages())
                .hasSize(4)
                .satisfiesExactly(
                        message -> assertThat(message).isInstanceOf(CohereUserMessageV2.class),
                        message -> assertThat(message).isInstanceOfSatisfying(CohereAssistantMessageV2.class,
                                assistant -> assertThat(assistant.getToolCalls())
                                        .singleElement()
                                        .isInstanceOfSatisfying(CohereToolCallV2.class, toolCall -> {
                                            assertThat(toolCall.getId()).isEqualTo("call-1");
                                            assertThat(toolCall.getType()).isEqualTo(CohereToolCallV2.Type.Function);
                                        })),
                        message -> assertThat(message).isInstanceOfSatisfying(CohereToolMessageV2.class,
                                toolMessage -> {
                                    assertThat(toolMessage.getToolCallId()).isEqualTo("call-1");
                                    assertThat(cohereV2MessageText(toolMessage)).isEqualTo("{\"temp\":55}");
                                }),
                        message -> assertThat(message).isInstanceOf(CohereUserMessageV2.class));
    }

    @Test
    void extractsGenericToolCallsFromResponse() {
        OracleGenAiChatOptions options = defaultOptions();
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(genericToolCallResponse());

        ChatResponse response = model(client, options)
                .call(new Prompt("What is the weather?"));

        assertThat(response.getResult().getOutput().getToolCalls())
                .singleElement()
                .satisfies(toolCall -> {
                    assertThat(toolCall.id()).isEqualTo("call-1");
                    assertThat(toolCall.name()).isEqualTo("weather");
                    assertThat(toolCall.arguments()).isEqualTo("{\"city\":\"Seattle\"}");
                });
    }

    @Test
    void extractsCohereV2ToolCallsFromResponse() {
        OracleGenAiChatOptions options = defaultOptions();
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(cohereV2ToolCallResponse());

        ChatResponse response = model(client, options)
                .call(new Prompt("What is the weather?"));

        assertThat(response.getResult().getOutput().getToolCalls())
                .singleElement()
                .satisfies(toolCall -> {
                    assertThat(toolCall.id()).isEqualTo("call-1");
                    assertThat(toolCall.name()).isEqualTo("weather");
                    assertThat(toolCall.arguments()).isEqualTo("{\"city\":\"Seattle\"}");
                });
    }

    @Test
    void executesToolCallsAndReturnsFinalModelResponse() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setToolCallbacks(List.of(tool("weather", false)));
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(
                genericToolCallResponse(), genericTextResponse("The weather is 55 degrees."));

        ChatResponse response = model(client, options)
                .call(new Prompt("What is the weather in Seattle?"));

        assertThat(response.getResult().getOutput().getText()).isEqualTo("The weather is 55 degrees.");
        assertThat(client.requests()).hasSize(2);
        GenericChatRequest secondRequest = (GenericChatRequest) client.requests().get(1).getChatDetails().getChatRequest();
        assertThat(secondRequest.getMessages())
                .anySatisfy(message -> assertThat(message).isInstanceOf(ToolMessage.class));
    }

    @Test
    void accumulatesUsageAcrossToolCallingLoop() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setToolCallbacks(List.of(tool("weather", false)));
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(
                genericToolCallResponse(usage(1, 2, 3)),
                genericTextResponse("The weather is 55 degrees.", usage(4, 5, 9)));

        ChatResponse response = model(client, options)
                .call(new Prompt("What is the weather in Seattle?"));

        assertThat(response.getMetadata().getUsage().getPromptTokens()).isEqualTo(5);
        assertThat(response.getMetadata().getUsage().getCompletionTokens()).isEqualTo(7);
        assertThat(response.getMetadata().getUsage().getTotalTokens()).isEqualTo(12);
    }

    @Test
    void returnsDirectToolResults() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setToolCallbacks(List.of(tool("weather", true)));
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(genericToolCallResponse());

        ChatResponse response = model(client, options)
                .call(new Prompt("What is the weather in Seattle?"));

        assertThat(response.getResult().getOutput().getText()).isEqualTo("{\"temp\":55}");
        assertThat(client.requests()).hasSize(1);
    }

    @Test
    void returnsDirectToolResultsWithToolCallUsage() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setToolCallbacks(List.of(tool("weather", true)));
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(
                genericToolCallResponse(usage(1, 2, 3)));

        ChatResponse response = model(client, options)
                .call(new Prompt("What is the weather in Seattle?"));

        assertThat(response.getResult().getOutput().getText()).isEqualTo("{\"temp\":55}");
        assertThat(response.getMetadata().getUsage().getPromptTokens()).isEqualTo(1);
        assertThat(response.getMetadata().getUsage().getCompletionTokens()).isEqualTo(2);
        assertThat(response.getMetadata().getUsage().getTotalTokens()).isEqualTo(3);
    }

    @Test
    void rejectsLegacyCohereToolDefinitions() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setApiFormat(GenAiApiFormat.COHERE);
        options.setToolCallbacks(List.of(tool("weather", false)));
        OracleGenAiChatModel model = model(CLIENT, options);

        assertThatThrownBy(() -> model.toBaseChatRequest(new Prompt("hello"), options))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("legacy Cohere");
    }

    @Test
    void rejectsLegacyCohereToolResponseMessages() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setApiFormat(GenAiApiFormat.COHERE);
        OracleGenAiChatModel model = model(CLIENT, options);
        ToolResponseMessage toolResponseMessage = ToolResponseMessage.builder()
                .responses(List.of(new ToolResponseMessage.ToolResponse("id", "tool", "{}")))
                .build();

        assertThatThrownBy(() -> model.toBaseChatRequest(new Prompt(toolResponseMessage), options))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("legacy Cohere");
    }

    @Test
    void rejectsLegacyCohereAssistantToolCalls() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setApiFormat(GenAiApiFormat.COHERE);
        OracleGenAiChatModel model = model(CLIENT, options);
        AssistantMessage assistantMessage = AssistantMessage.builder()
                .content("calling tool")
                .toolCalls(List.of(new AssistantMessage.ToolCall("id", "function", "tool", "{}")))
                .build();

        assertThatThrownBy(() -> model.toBaseChatRequest(new Prompt(assistantMessage), options))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("legacy Cohere");
    }

    @Test
    void createsStreamingRequestsForAllApiFormats() {
        assertStreamingRequest(GenAiApiFormat.GENERIC, GenericChatRequest.class);
        assertStreamingRequest(GenAiApiFormat.COHERE_V2, CohereChatRequestV2.class);
        assertStreamingRequest(GenAiApiFormat.COHERE, CohereChatRequest.class);
    }

    @Test
    void emitsStreamingTextChunksInOrder() {
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(streamResponse(
                sseStream(genericTextEvent("Hel"), genericTextEvent("lo"))));

        List<ChatResponse> responses = stream(model(client, defaultOptions()), "hello");

        assertThat(responses)
                .extracting(response -> response.getResult().getOutput().getText())
                .containsExactly("Hel", "lo");
    }

    @Test
    void observesChatStream() {
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(streamResponse(
                sseStream(genericTextEvent("Hel"), genericTextEvent("lo"))));
        RecordingObservationHandler handler = new RecordingObservationHandler(ChatModelObservationContext.class);
        ObservationRegistry observationRegistry = ObservationRegistry.create();
        observationRegistry.observationConfig().observationHandler(handler);
        OracleGenAiChatModel model = OracleGenAiChatModel.builder()
                .client(client)
                .defaultOptions(defaultOptions())
                .observationRegistry(observationRegistry)
                .build();

        List<ChatResponse> responses = stream(model, "hello");

        assertThat(responses)
                .extracting(response -> response.getResult().getOutput().getText())
                .containsExactly("Hel", "lo");
        assertThat(handler.stoppedContexts())
                .singleElement()
                .isInstanceOfSatisfying(ChatModelObservationContext.class, context -> {
                    assertThat(context.getOperationMetadata().provider()).isEqualTo("oci_genai");
                    assertThat(context.isStreaming()).isTrue();
                    assertThat(context.getRequest().getContents()).isEqualTo("hello");
                    assertThat(context.getResponse().getResult().getOutput().getText()).isEqualTo("Hello");
                });
    }

    @Test
    void closesStreamingResponseOnComplete() {
        CloseTrackingInputStream eventStream = sseStream(genericTextEvent("done"));
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(streamResponse(eventStream));

        stream(model(client, defaultOptions()), "hello");

        assertClosed(eventStream);
    }

    @Test
    void closesStreamingResponseOnError() {
        CloseTrackingInputStream eventStream = sseStream("not-json");

        assertThatThrownBy(() -> new ChatStreamResponseConverter().toChatResponses(streamResponse(eventStream))
                .collectList()
                .block(Duration.ofSeconds(5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to parse OCI Generative AI streaming chat event");
        assertClosed(eventStream);
    }

    @Test
    void closesStreamingResponseOnCancellation() {
        CloseTrackingInputStream eventStream = sseStream(genericTextEvent("first"), genericTextEvent("second"));
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(streamResponse(eventStream));

        model(client, defaultOptions()).stream(new Prompt("hello"))
                .take(1)
                .collectList()
                .block(Duration.ofSeconds(5));

        assertClosed(eventStream);
    }

    @Test
    void rejectsStreamingResponseWithoutEventStream() {
        assertThatThrownBy(() -> new ChatStreamResponseConverter()
                .toChatResponses(com.oracle.bmc.generativeaiinference.responses.ChatResponse.builder().build())
                .collectList()
                .block(Duration.ofSeconds(5)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("event stream");
    }

    @Test
    void parsesEscapedStreamingText() {
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(streamResponse(
                sseStream(genericTextEvent("line\\nbreak \\\"quoted\\\""))));

        List<ChatResponse> responses = stream(model(client, defaultOptions()), "hello");

        assertThat(responses.get(0).getResult().getOutput().getText()).isEqualTo("line\nbreak \"quoted\"");
    }

    @Test
    void parsesMultilineSseDataEvents() {
        CloseTrackingInputStream eventStream = new CloseTrackingInputStream("""
                data: {"chatResponse":{"choices":[{"message":{"content":[
                data: {"text":"hello"}
                data: ]}}]}}

                """);
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(streamResponse(eventStream));

        List<ChatResponse> responses = stream(model(client, defaultOptions()), "hello");

        assertThat(responses.get(0).getResult().getOutput().getText()).isEqualTo("hello");
    }

    @Test
    void parsesStreamingUsageOnlyEvents() {
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(streamResponse(sseStream("""
                {"chatResponse":{"usage":{"promptTokens":2,"completionTokens":3,"totalTokens":5}}}
                """)));

        List<ChatResponse> responses = stream(model(client, defaultOptions()), "hello");

        assertThat(responses.get(0).getMetadata().getUsage().getPromptTokens()).isEqualTo(2);
        assertThat(responses.get(0).getMetadata().getUsage().getCompletionTokens()).isEqualTo(3);
        assertThat(responses.get(0).getMetadata().getUsage().getTotalTokens()).isEqualTo(5);
    }

    @Test
    void parsesStreamingFinishReasons() {
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(streamResponse(sseStream("""
                {"chatResponse":{"choices":[{"message":{"content":[{"text":"done"}]},"finishReason":"stop"}]}}
                """)));

        List<ChatResponse> responses = stream(model(client, defaultOptions()), "hello");

        assertThat(responses.get(0).getResult().getMetadata().getFinishReason()).isEqualTo("stop");
    }

    @Test
    void executesStreamingToolCallsAndReturnsDirectResult() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setToolCallbacks(List.of(tool("weather", true)));
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(
                streamResponse(sseStream(genericTextEvent("checking"), genericToolCallEvent())));

        List<ChatResponse> responses = stream(model(client, options),
                "What is the weather in Seattle?");

        assertThat(responses)
                .extracting(response -> response.getResult().getOutput().getText())
                .containsExactly("{\"temp\":55}");
        assertThat(client.requests()).hasSize(1);
    }

    @Test
    void executesStreamingToolCallsAndStreamsFinalModelResponse() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setToolCallbacks(List.of(tool("weather", false)));
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(
                streamResponse(sseStream(genericTextEvent("checking"), genericToolCallEvent())),
                streamResponse(sseStream(genericTextEvent("The weather "), genericTextEvent("is 55 degrees."))));

        List<ChatResponse> responses = stream(model(client, options),
                "What is the weather in Seattle?");

        assertThat(responses)
                .extracting(response -> response.getResult().getOutput().getText())
                .containsExactly("The weather ", "is 55 degrees.");
        assertThat(client.requests()).hasSize(2);
        GenericChatRequest secondRequest = (GenericChatRequest) client.requests().get(1).getChatDetails().getChatRequest();
        assertThat(secondRequest.getMessages())
                .anySatisfy(message -> assertThat(message).isInstanceOf(ToolMessage.class));
    }

    @Test
    void replaysBufferedStreamingChunksWhenNoToolCallIsRequired() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setToolCallbacks(List.of(tool("weather", false)));
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(streamResponse(
                sseStream(genericTextEvent("Hel"), genericTextEvent("lo"))));

        List<ChatResponse> responses = stream(model(client, options), "hello");

        assertThat(responses)
                .extracting(response -> response.getResult().getOutput().getText())
                .containsExactly("Hel", "lo");
    }

    private static OracleGenAiChatModel model(CapturingGenerativeAiInference client,
            OracleGenAiChatOptions defaultOptions) {
        return OracleGenAiChatModel.builder()
                .client(client)
                .defaultOptions(defaultOptions)
                .build();
    }

    private static OracleGenAiChatModel model(NoOpGenerativeAiInference client,
            OracleGenAiChatOptions defaultOptions) {
        return OracleGenAiChatModel.builder()
                .client(client)
                .defaultOptions(defaultOptions)
                .build();
    }

    private static OracleGenAiChatOptions defaultOptions() {
        return OracleGenAiChatOptions.builder()
                .compartmentId("compartment")
                .model("model")
                .temperature(0.2)
                .maxTokens(20)
                .build();
    }

    private static Prompt chatHistoryPrompt() {
        return new Prompt(List.of(
                new SystemMessage("answer briefly"),
                new UserMessage("hello"),
                new AssistantMessage("hi there"),
                new UserMessage("what did I say?")));
    }

    private static Prompt toolConversationPrompt() {
        return new Prompt(List.of(
                new UserMessage("weather in Seattle?"),
                AssistantMessage.builder()
                        .content("")
                        .toolCalls(List.of(new AssistantMessage.ToolCall("call-1", "function", "weather",
                                "{\"city\":\"Seattle\"}")))
                        .build(),
                ToolResponseMessage.builder()
                        .responses(List.of(new ToolResponseMessage.ToolResponse("call-1", "weather", "{\"temp\":55}")))
                        .build(),
                new UserMessage("summarize it")));
    }

    private static void assertStreamingRequest(GenAiApiFormat apiFormat,
            Class<? extends BaseChatRequest> requestType) {
        OracleGenAiChatOptions options = defaultOptions();
        options.setApiFormat(apiFormat);
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(
                streamResponse(sseStream(genericTextEvent("hello"))));

        stream(model(client, options), "hello");

        BaseChatRequest request = client.requests().get(0).getChatDetails().getChatRequest();
        assertThat(request).isInstanceOf(requestType);
        if (request instanceof GenericChatRequest genericRequest) {
            assertThat(genericRequest.getIsStream()).isTrue();
            assertThat(genericRequest.getStreamOptions().getIsIncludeUsage()).isTrue();
        }
        else if (request instanceof CohereChatRequestV2 cohereV2Request) {
            assertThat(cohereV2Request.getIsStream()).isTrue();
            assertThat(cohereV2Request.getStreamOptions().getIsIncludeUsage()).isTrue();
        }
        else if (request instanceof CohereChatRequest cohereRequest) {
            assertThat(cohereRequest.getIsStream()).isTrue();
            assertThat(cohereRequest.getStreamOptions().getIsIncludeUsage()).isTrue();
        }
    }

    private static List<ChatResponse> stream(OracleGenAiChatModel model, String prompt) {
        return model.stream(new Prompt(prompt)).collectList().block(Duration.ofSeconds(5));
    }

    private static void assertClosed(CloseTrackingInputStream eventStream) {
        long deadline = System.nanoTime() + Duration.ofSeconds(1).toNanos();
        while (!eventStream.closed() && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        assertThat(eventStream.closed()).isTrue();
    }

    private static String genericMessageText(com.oracle.bmc.generativeaiinference.model.Message message) {
        ChatContent content = singleContent(message.getContent());
        assertThat(content).isInstanceOf(TextContent.class);
        return ((TextContent) content).getText();
    }

    private static String cohereV2MessageText(CohereMessageV2 message) {
        CohereContentV2 content = singleContent(message.getContent());
        assertThat(content).isInstanceOf(CohereTextContentV2.class);
        return ((CohereTextContentV2) content).getText();
    }

    private static ToolCallback tool(String name, boolean returnDirect) {
        return new ToolCallback() {
            @Override
            public ToolDefinition getToolDefinition() {
                return ToolDefinition.builder()
                        .name(name)
                        .description("Weather lookup")
                        .inputSchema("""
                                {"type":"object","properties":{"city":{"type":"string"}},"required":["city"]}
                                """)
                        .build();
            }

            @Override
            public ToolMetadata getToolMetadata() {
                return ToolMetadata.builder().returnDirect(returnDirect).build();
            }

            @Override
            public String call(String toolInput) {
                return "{\"temp\":55}";
            }
        };
    }

    private static com.oracle.bmc.generativeaiinference.responses.ChatResponse genericToolCallResponse() {
        return genericToolCallResponse(null);
    }

    private static com.oracle.bmc.generativeaiinference.responses.ChatResponse genericToolCallResponse(Usage usage) {
        com.oracle.bmc.generativeaiinference.model.AssistantMessage assistantMessage =
                com.oracle.bmc.generativeaiinference.model.AssistantMessage.builder()
                        .toolCalls(List.of(FunctionCall.builder()
                                .id("call-1")
                                .name("weather")
                                .arguments("{\"city\":\"Seattle\"}")
                                .build()))
                        .build();
        return ociChatResponse(GenericChatResponse.builder()
                .choices(List.of(com.oracle.bmc.generativeaiinference.model.ChatChoice.builder()
                        .message(assistantMessage)
                        .finishReason("tool_calls")
                        .usage(usage)
                        .build()))
                .build());
    }

    private static com.oracle.bmc.generativeaiinference.responses.ChatResponse genericTextResponse(String text) {
        return genericTextResponse(text, null);
    }

    private static com.oracle.bmc.generativeaiinference.responses.ChatResponse genericTextResponse(String text,
            Usage usage) {
        com.oracle.bmc.generativeaiinference.model.AssistantMessage assistantMessage =
                com.oracle.bmc.generativeaiinference.model.AssistantMessage.builder()
                        .content(List.of(TextContent.builder().text(text).build()))
                        .build();
        return ociChatResponse(GenericChatResponse.builder()
                .choices(List.of(com.oracle.bmc.generativeaiinference.model.ChatChoice.builder()
                        .message(assistantMessage)
                        .finishReason("stop")
                        .usage(usage)
                        .build()))
                .build());
    }

    private static Usage usage(int promptTokens, int completionTokens, int totalTokens) {
        return Usage.builder()
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(totalTokens)
                .build();
    }

    private static com.oracle.bmc.generativeaiinference.responses.ChatResponse cohereV2ToolCallResponse() {
        return ociChatResponse(com.oracle.bmc.generativeaiinference.model.CohereChatResponseV2.builder()
                .message(CohereAssistantMessageV2.builder()
                        .toolCalls(List.of(CohereToolCallV2.builder()
                                .id("call-1")
                                .type(CohereToolCallV2.Type.Function)
                                .function(FunctionCall.builder()
                                        .name("weather")
                                        .arguments("{\"city\":\"Seattle\"}")
                                        .build())
                                .build()))
                        .build())
                .build());
    }

    private static com.oracle.bmc.generativeaiinference.responses.ChatResponse ociChatResponse(
            BaseChatResponse chatResponse) {
        return com.oracle.bmc.generativeaiinference.responses.ChatResponse.builder()
                .chatResult(ChatResult.builder()
                        .modelId("model")
                        .chatResponse(chatResponse)
                        .build())
                .build();
    }

    private static com.oracle.bmc.generativeaiinference.responses.ChatResponse streamResponse(
            CloseTrackingInputStream eventStream) {
        return com.oracle.bmc.generativeaiinference.responses.ChatResponse.builder()
                .opcRequestId("opc-request")
                .eventStream(eventStream)
                .build();
    }

    private static CloseTrackingInputStream sseStream(String... events) {
        StringBuilder stream = new StringBuilder();
        for (String event : events) {
            stream.append("data: ").append(event.strip()).append("\n\n");
        }
        return new CloseTrackingInputStream(stream.toString());
    }

    private static String genericTextEvent(String text) {
        return """
                {"modelId":"model","chatResponse":{"choices":[{"message":{"content":[{"text":"%s"}]}}]}}
                """.formatted(text);
    }

    private static String genericToolCallEvent() {
        return """
                {"modelId":"model","chatResponse":{"choices":[{"message":{"toolCalls":[{"id":"call-1","type":"function","name":"weather","arguments":"{\\\"city\\\":\\\"Seattle\\\"}"}]},"finishReason":"tool_calls"}]}}
                """;
    }

    private static String legacyCohereMessageText(CohereMessage message) {
        if (message instanceof CohereUserMessage userMessage) {
            return userMessage.getMessage();
        }
        if (message instanceof CohereChatBotMessage botMessage) {
            return botMessage.getMessage();
        }
        throw new AssertionError("Unexpected legacy Cohere message type: " + message.getClass().getName());
    }

    private static <T> T singleContent(List<T> content) {
        assertThat(content).singleElement();
        return content.get(0);
    }

    private static final class FailingGenerativeAiInference extends NoOpGenerativeAiInference {

        private final RuntimeException failure;

        private FailingGenerativeAiInference(RuntimeException failure) {
            this.failure = failure;
        }

        @Override
        public com.oracle.bmc.generativeaiinference.responses.ChatResponse chat(ChatRequest request) {
            throw failure;
        }
    }

    private static final class CapturingGenerativeAiInference extends NoOpGenerativeAiInference {

        private final Queue<com.oracle.bmc.generativeaiinference.responses.ChatResponse> responses = new ArrayDeque<>();

        private final List<ChatRequest> requests = new ArrayList<>();

        private CapturingGenerativeAiInference(com.oracle.bmc.generativeaiinference.responses.ChatResponse... responses) {
            this.responses.addAll(List.of(responses));
        }

        @Override
        public com.oracle.bmc.generativeaiinference.responses.ChatResponse chat(ChatRequest request) {
            requests.add(request);
            return responses.remove();
        }

        private List<ChatRequest> requests() {
            return requests;
        }
    }

    private static final class RecordingObservationHandler implements ObservationHandler<Observation.Context> {

        private final Class<? extends Observation.Context> contextType;

        private final List<Observation.Context> stoppedContexts = new ArrayList<>();

        private final List<Observation.Context> errorContexts = new ArrayList<>();

        private RecordingObservationHandler(Class<? extends Observation.Context> contextType) {
            this.contextType = contextType;
        }

        @Override
        public void onError(Observation.Context context) {
            errorContexts.add(context);
        }

        @Override
        public void onStop(Observation.Context context) {
            stoppedContexts.add(context);
        }

        @Override
        public boolean supportsContext(Observation.Context context) {
            return contextType.isInstance(context);
        }

        private List<Observation.Context> stoppedContexts() {
            return stoppedContexts;
        }

        private List<Observation.Context> errorContexts() {
            return errorContexts;
        }
    }

    private static final class CloseTrackingInputStream extends ByteArrayInputStream {

        private volatile boolean closed;

        private CloseTrackingInputStream(String value) {
            super(value.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }

        private boolean closed() {
            return closed;
        }
    }

}
