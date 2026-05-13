/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.oracle.bmc.generativeaiinference.model.BaseChatRequest;
import com.oracle.bmc.generativeaiinference.model.ChatContent;
import com.oracle.bmc.generativeaiinference.model.ChatResult;
import com.oracle.bmc.generativeaiinference.model.CohereAssistantMessageV2;
import com.oracle.bmc.generativeaiinference.model.CohereChatBotMessage;
import com.oracle.bmc.generativeaiinference.model.CohereChatRequest;
import com.oracle.bmc.generativeaiinference.model.CohereChatRequestV2;
import com.oracle.bmc.generativeaiinference.model.CohereContentV2;
import com.oracle.bmc.generativeaiinference.model.CohereToolCallV2;
import com.oracle.bmc.generativeaiinference.model.CohereToolMessageV2;
import com.oracle.bmc.generativeaiinference.model.CohereToolV2;
import com.oracle.bmc.generativeaiinference.model.CohereMessage;
import com.oracle.bmc.generativeaiinference.model.CohereMessageV2;
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
import com.oracle.bmc.generativeaiinference.model.BaseChatResponse;
import com.oracle.bmc.generativeaiinference.requests.ChatRequest;
import com.oracle.spring.ai.oracle.api.GenAiApiFormat;
import com.oracle.spring.ai.oracle.api.OracleGenAiServingMode;
import com.oracle.spring.ai.oracle.test.NoOpGenerativeAiInference;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OracleGenAiChatModelTests {

    private static final NoOpGenerativeAiInference CLIENT = new NoOpGenerativeAiInference();

    @Test
    void createsGenericRequest() {
        BaseChatRequest request = new OracleGenAiChatModel(CLIENT, defaultOptions())
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
        BaseChatRequest request = new OracleGenAiChatModel(CLIENT, defaultOptions())
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

        BaseChatRequest request = new OracleGenAiChatModel(CLIENT, options)
                .toBaseChatRequest(new Prompt(List.of(new SystemMessage("rules"), new UserMessage("hello"))), options);

        assertThat(request).isInstanceOf(CohereChatRequestV2.class);
        assertThat(((CohereChatRequestV2) request).getMessages()).hasSize(2);
    }

    @Test
    void createsCohereV2RequestWithChatHistory() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setApiFormat(GenAiApiFormat.COHERE_V2);

        BaseChatRequest request = new OracleGenAiChatModel(CLIENT, options)
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

        BaseChatRequest request = new OracleGenAiChatModel(CLIENT, options)
                .toBaseChatRequest(new Prompt("hello"), options);

        assertThat(request).isInstanceOf(CohereChatRequestV2.class);
    }

    @Test
    void infersLegacyCohereRequestForCommandRModels() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setModel("cohere.command-r-plus-08-2024");

        BaseChatRequest request = new OracleGenAiChatModel(CLIENT, options)
                .toBaseChatRequest(new Prompt("hello"), options);

        assertThat(request).isInstanceOf(CohereChatRequest.class);
    }

    @Test
    void createsLegacyCohereRequest() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setApiFormat(GenAiApiFormat.COHERE);

        BaseChatRequest request = new OracleGenAiChatModel(CLIENT, options)
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

        BaseChatRequest request = new OracleGenAiChatModel(CLIENT, options)
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
        OracleGenAiChatModel model = new OracleGenAiChatModel(CLIENT, options);

        assertThatThrownBy(() -> model.toChatRequest(new Prompt("hello"), options))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("options.model");
    }

    @Test
    void validatesDedicatedEndpointId() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setServingMode(OracleGenAiServingMode.DEDICATED);
        options.setEndpointId(null);
        OracleGenAiChatModel model = new OracleGenAiChatModel(CLIENT, options);

        assertThatThrownBy(() -> model.toChatRequest(new Prompt("hello"), options))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("options.endpointId");
    }

    @Test
    void createsDedicatedServingMode() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setServingMode(OracleGenAiServingMode.DEDICATED);
        options.setEndpointId("endpoint");

        ChatRequest request = new OracleGenAiChatModel(CLIENT, options)
                .toChatRequest(new Prompt("hello"), options);

        assertThat(request.getChatDetails().getServingMode())
                .isInstanceOf(com.oracle.bmc.generativeaiinference.model.DedicatedServingMode.class);
    }

    @Test
    void copiesAndMergesToolCallingOptions() {
        ToolCallback defaultTool = tool("weather", false);
        ToolCallback runtimeTool = tool("time", false);
        OracleGenAiChatOptions defaults = defaultOptions();
        defaults.setToolCallbacks(List.of(defaultTool));
        defaults.setToolNames(Set.of("defaultName"));
        defaults.setToolContext(Map.of("default", "value"));
        defaults.setInternalToolExecutionEnabled(false);
        OracleGenAiChatOptions runtime = OracleGenAiChatOptions.builder()
                .toolCallbacks(List.of(runtimeTool))
                .toolNames(Set.of("runtimeName"))
                .toolContext(Map.of("runtime", "value"))
                .internalToolExecutionEnabled(true)
                .build();

        OracleGenAiChatOptions merged = defaults.merge(runtime);

        assertThat(merged.getToolCallbacks()).containsExactly(runtimeTool);
        assertThat(merged.getToolNames()).containsExactly("runtimeName");
        assertThat(merged.getToolContext()).containsEntry("default", "value").containsEntry("runtime", "value");
        assertThat(merged.getInternalToolExecutionEnabled()).isTrue();
    }

    @Test
    void createsGenericRequestWithToolsAndToolMessages() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setToolCallbacks(List.of(tool("weather", false)));

        BaseChatRequest request = new OracleGenAiChatModel(CLIENT, options)
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

        BaseChatRequest request = new OracleGenAiChatModel(CLIENT, options)
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
        options.setInternalToolExecutionEnabled(false);
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(genericToolCallResponse());

        ChatResponse response = new OracleGenAiChatModel(client, options)
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
        options.setInternalToolExecutionEnabled(false);
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(cohereV2ToolCallResponse());

        ChatResponse response = new OracleGenAiChatModel(client, options)
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

        ChatResponse response = new OracleGenAiChatModel(client, options)
                .call(new Prompt("What is the weather in Seattle?"));

        assertThat(response.getResult().getOutput().getText()).isEqualTo("The weather is 55 degrees.");
        assertThat(client.requests()).hasSize(2);
        GenericChatRequest secondRequest = (GenericChatRequest) client.requests().get(1).getChatDetails().getChatRequest();
        assertThat(secondRequest.getMessages())
                .anySatisfy(message -> assertThat(message).isInstanceOf(ToolMessage.class));
    }

    @Test
    void returnsDirectToolResults() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setToolCallbacks(List.of(tool("weather", true)));
        CapturingGenerativeAiInference client = new CapturingGenerativeAiInference(genericToolCallResponse());

        ChatResponse response = new OracleGenAiChatModel(client, options)
                .call(new Prompt("What is the weather in Seattle?"));

        assertThat(response.getResult().getOutput().getText()).isEqualTo("{\"temp\":55}");
        assertThat(client.requests()).hasSize(1);
    }

    @Test
    void rejectsLegacyCohereToolDefinitions() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setApiFormat(GenAiApiFormat.COHERE);
        options.setToolCallbacks(List.of(tool("weather", false)));
        OracleGenAiChatModel model = new OracleGenAiChatModel(CLIENT, options);

        assertThatThrownBy(() -> model.toBaseChatRequest(new Prompt("hello"), options))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("legacy Cohere");
    }

    @Test
    void rejectsLegacyCohereToolResponseMessages() {
        OracleGenAiChatOptions options = defaultOptions();
        options.setApiFormat(GenAiApiFormat.COHERE);
        OracleGenAiChatModel model = new OracleGenAiChatModel(CLIENT, options);
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
        OracleGenAiChatModel model = new OracleGenAiChatModel(CLIENT, options);
        AssistantMessage assistantMessage = AssistantMessage.builder()
                .content("calling tool")
                .toolCalls(List.of(new AssistantMessage.ToolCall("id", "function", "tool", "{}")))
                .build();

        assertThatThrownBy(() -> model.toBaseChatRequest(new Prompt(assistantMessage), options))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("legacy Cohere");
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
                        .build()))
                .build());
    }

    private static com.oracle.bmc.generativeaiinference.responses.ChatResponse genericTextResponse(String text) {
        com.oracle.bmc.generativeaiinference.model.AssistantMessage assistantMessage =
                com.oracle.bmc.generativeaiinference.model.AssistantMessage.builder()
                        .content(List.of(TextContent.builder().text(text).build()))
                        .build();
        return ociChatResponse(GenericChatResponse.builder()
                .choices(List.of(com.oracle.bmc.generativeaiinference.model.ChatChoice.builder()
                        .message(assistantMessage)
                        .finishReason("stop")
                        .build()))
                .build());
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

}
