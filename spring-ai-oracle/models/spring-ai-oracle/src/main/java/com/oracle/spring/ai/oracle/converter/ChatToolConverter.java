/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle.converter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.oracle.bmc.generativeaiinference.model.CohereToolCallV2;
import com.oracle.bmc.generativeaiinference.model.CohereToolV2;
import com.oracle.bmc.generativeaiinference.model.Function;
import com.oracle.bmc.generativeaiinference.model.FunctionCall;
import com.oracle.bmc.generativeaiinference.model.FunctionDefinition;
import com.oracle.bmc.generativeaiinference.model.ToolCall;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.util.json.JsonParser;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public final class ChatToolConverter {

    private ChatToolConverter() {
    }

    public static List<com.oracle.bmc.generativeaiinference.model.ToolDefinition> toGenericTools(
            List<ToolDefinition> toolDefinitions) {
        if (CollectionUtils.isEmpty(toolDefinitions)) {
            return null;
        }
        return toolDefinitions.stream()
                .map(toolDefinition -> (com.oracle.bmc.generativeaiinference.model.ToolDefinition) FunctionDefinition.builder()
                        .name(toolDefinition.name())
                        .description(toolDefinition.description())
                        .parameters(toJsonObject(toolDefinition.inputSchema()))
                        .build())
                .toList();
    }

    public static List<CohereToolV2> toCohereV2Tools(List<ToolDefinition> toolDefinitions) {
        if (CollectionUtils.isEmpty(toolDefinitions)) {
            return null;
        }
        return toolDefinitions.stream()
                .map(toolDefinition -> CohereToolV2.builder()
                        .type(CohereToolV2.Type.Function)
                        .function(Function.builder()
                                .name(toolDefinition.name())
                                .description(toolDefinition.description())
                                .parameters(toJsonObject(toolDefinition.inputSchema()))
                                .build())
                        .build())
                .toList();
    }

    public static List<ToolCall> toGenericToolCalls(List<AssistantMessage.ToolCall> toolCalls) {
        if (CollectionUtils.isEmpty(toolCalls)) {
            return null;
        }
        return toolCalls.stream()
                .map(toolCall -> {
                    assertFunctionToolCall(toolCall);
                    return (ToolCall) FunctionCall.builder()
                            .id(toolCall.id())
                            .name(toolCall.name())
                            .arguments(Objects.requireNonNullElse(toolCall.arguments(), "{}"))
                            .build();
                })
                .toList();
    }

    public static List<CohereToolCallV2> toCohereV2ToolCalls(List<AssistantMessage.ToolCall> toolCalls) {
        if (CollectionUtils.isEmpty(toolCalls)) {
            return null;
        }
        return toolCalls.stream()
                .map(toolCall -> {
                    assertFunctionToolCall(toolCall);
                    return CohereToolCallV2.builder()
                            .id(toolCall.id())
                            .type(CohereToolCallV2.Type.Function)
                            .function(FunctionCall.builder()
                                    .name(toolCall.name())
                                    .arguments(Objects.requireNonNullElse(toolCall.arguments(), "{}"))
                                    .build())
                            .build();
                })
                .toList();
    }

    public static List<AssistantMessage.ToolCall> fromGenericToolCalls(List<ToolCall> toolCalls) {
        if (CollectionUtils.isEmpty(toolCalls)) {
            return Collections.emptyList();
        }
        return toolCalls.stream()
                .map(ChatToolConverter::toSpringToolCall)
                .toList();
    }

    public static List<AssistantMessage.ToolCall> fromCohereV2ToolCalls(List<CohereToolCallV2> toolCalls) {
        if (CollectionUtils.isEmpty(toolCalls)) {
            return Collections.emptyList();
        }
        return toolCalls.stream()
                .map(toolCall -> new AssistantMessage.ToolCall(toolCall.getId(), "function",
                        cohereV2FunctionName(toolCall.getFunction()), cohereV2FunctionArguments(toolCall.getFunction())))
                .toList();
    }

    public static void assertFunctionToolCall(AssistantMessage.ToolCall toolCall) {
        if (StringUtils.hasText(toolCall.type()) && !"function".equals(toolCall.type())) {
            throw new UnsupportedOperationException("OCI Generative AI only supports function tool calls.");
        }
    }

    public static UnsupportedOperationException legacyCohereToolCallingUnsupported() {
        return new UnsupportedOperationException(
                "OCI Generative AI legacy Cohere chat tool calling is not supported. Use GENERIC or COHERE_V2 apiFormat.");
    }

    private static AssistantMessage.ToolCall toSpringToolCall(ToolCall toolCall) {
        if (toolCall instanceof FunctionCall functionCall) {
            return new AssistantMessage.ToolCall(functionCall.getId(), "function", functionCall.getName(),
                    Objects.requireNonNullElse(functionCall.getArguments(), "{}"));
        }
        throw new IllegalStateException("Unsupported OCI Generative AI tool call type: " + toolCall.getClass().getName());
    }

    private static Object toJsonObject(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyMap();
        }
        return JsonParser.fromJson(json, Object.class);
    }

    private static String cohereV2FunctionName(Object function) {
        if (function instanceof FunctionCall functionCall) {
            return functionCall.getName();
        }
        if (function instanceof Map<?, ?> map && map.get("name") != null) {
            return map.get("name").toString();
        }
        throw new IllegalStateException("OCI Generative AI Cohere V2 tool call did not include a function name.");
    }

    private static String cohereV2FunctionArguments(Object function) {
        if (function instanceof FunctionCall functionCall) {
            return Objects.requireNonNullElse(functionCall.getArguments(), "{}");
        }
        if (function instanceof Map<?, ?> map) {
            Object arguments = map.get("arguments");
            if (arguments == null) {
                return "{}";
            }
            if (arguments instanceof String text) {
                return text;
            }
            return ModelOptionsUtils.toJsonString(arguments);
        }
        throw new IllegalStateException("OCI Generative AI Cohere V2 tool call did not include function arguments.");
    }

}
