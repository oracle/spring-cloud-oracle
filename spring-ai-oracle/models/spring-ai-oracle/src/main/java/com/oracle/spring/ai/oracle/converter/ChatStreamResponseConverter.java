/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle.converter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

/**
 * Converts OCI Generative AI server-sent chat events into Spring AI streaming responses.
 */
public final class ChatStreamResponseConverter {

    private static final String DONE = "[DONE]";

    public Flux<ChatResponse> toChatResponses(
            com.oracle.bmc.generativeaiinference.responses.ChatResponse response) {
        InputStream eventStream = response.getEventStream();
        if (eventStream == null) {
            return Flux.error(new IllegalStateException(
                    "OCI Generative AI streaming chat response did not contain an event stream."));
        }
        return Flux.create(sink -> readEventStream(eventStream, response, sink), FluxSink.OverflowStrategy.BUFFER);
    }

    private void readEventStream(InputStream eventStream,
            com.oracle.bmc.generativeaiinference.responses.ChatResponse response, FluxSink<ChatResponse> sink) {
        Disposable close = () -> closeQuietly(eventStream);
        sink.onCancel(close);
        sink.onDispose(close);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(eventStream, StandardCharsets.UTF_8))) {
            StringBuilder eventData = new StringBuilder();
            String line;
            while (!sink.isCancelled() && (line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    if (dispatchEvent(eventData, response, sink)) {
                        return;
                    }
                    eventData.setLength(0);
                    continue;
                }
                if (line.startsWith(":")) {
                    continue;
                }
                if (line.startsWith("data:")) {
                    appendDataLine(eventData, line.substring("data:".length()));
                }
                else if (line.startsWith("{")) {
                    appendDataLine(eventData, line);
                }
            }
            if (!sink.isCancelled() && dispatchEvent(eventData, response, sink)) {
                return;
            }
            if (!sink.isCancelled()) {
                sink.complete();
            }
        }
        catch (IOException | RuntimeException ex) {
            if (!sink.isCancelled()) {
                sink.error(ex);
            }
        }
        finally {
            close.dispose();
        }
    }

    private static void appendDataLine(StringBuilder eventData, String data) {
        if (eventData.length() > 0) {
            eventData.append('\n');
        }
        if (data.startsWith(" ")) {
            eventData.append(data.substring(1));
        }
        else {
            eventData.append(data);
        }
    }

    private boolean dispatchEvent(StringBuilder eventData,
            com.oracle.bmc.generativeaiinference.responses.ChatResponse response, FluxSink<ChatResponse> sink) {
        if (eventData.isEmpty()) {
            return false;
        }
        String data = eventData.toString().trim();
        if (data.isEmpty()) {
            return false;
        }
        if (DONE.equals(data)) {
            sink.complete();
            return true;
        }
        sink.next(toChatResponse(data, response));
        return false;
    }

    private static ChatResponse toChatResponse(String data,
            com.oracle.bmc.generativeaiinference.responses.ChatResponse response) {
        Map<String, Object> event = parseEvent(data);
        ExtractedChunk chunk = extractChunk(event);
        ChatGenerationMetadata generationMetadata = ChatGenerationMetadata.builder()
                .finishReason(chunk.finishReason())
                .build();
        AssistantMessage assistantMessage = AssistantMessage.builder()
                .content(chunk.text())
                .toolCalls(chunk.toolCalls())
                .build();
        Generation generation = new Generation(assistantMessage, generationMetadata);
        ChatResponseMetadata metadata = ChatResponseMetadata.builder()
                .id(chunk.id())
                .model(chunk.model())
                .usage(chunk.usage())
                .keyValue("modelVersion", chunk.modelVersion())
                .keyValue("opcRequestId", response.getOpcRequestId())
                .build();
        return new ChatResponse(Collections.singletonList(generation), metadata);
    }

    private static Map<String, Object> parseEvent(String data) {
        try {
            return ModelOptionsUtils.jsonToMap(data);
        }
        catch (RuntimeException ex) {
            throw new IllegalArgumentException("Failed to parse OCI Generative AI streaming chat event.", ex);
        }
    }

    private static ExtractedChunk extractChunk(Map<String, Object> event) {
        Map<String, Object> chatResult = mapValue(event, "chatResult", "chat_result");
        Map<String, Object> chatResponse = firstMap(mapValue(chatResult, "chatResponse", "chat_response"),
                mapValue(event, "chatResponse", "chat_response"), event);
        Map<String, Object> choice = lastMap(listValue(chatResponse, "choices"));
        Map<String, Object> message = firstMap(mapValue(choice, "delta"), mapValue(choice, "message"),
                mapValue(chatResponse, "message"), mapValue(event, "delta"), mapValue(event, "message"));

        String text = firstNonNull(textFromMessage(message), textFromChoice(choice), stringValue(chatResponse, "text"),
                stringValue(event, "text"));
        List<AssistantMessage.ToolCall> toolCalls = firstNonEmpty(toolCallsFromMessage(message),
                toolCallsFromMessage(chatResponse), toolCallsFromMessage(event));
        String finishReason = firstNonNull(stringValue(choice, "finishReason", "finish_reason"),
                stringValue(chatResponse, "finishReason", "finish_reason"),
                stringValue(event, "finishReason", "finish_reason"));
        Map<String, Object> usageMap = firstMap(mapValue(choice, "usage"), mapValue(chatResponse, "usage"),
                mapValue(event, "usage"));

        return new ExtractedChunk(
                firstNonNull(stringValue(chatResponse, "id"), stringValue(event, "id")),
                firstNonNull(stringValue(chatResult, "modelId", "model_id"), stringValue(event, "modelId", "model_id"),
                        stringValue(event, "model")),
                firstNonNull(stringValue(chatResult, "modelVersion", "model_version"),
                        stringValue(event, "modelVersion", "model_version")),
                Objects.requireNonNullElse(text, ""), finishReason, toUsage(usageMap), toolCalls);
    }

    private static String textFromChoice(Map<String, Object> choice) {
        if (choice == null) {
            return null;
        }
        return firstNonNull(stringValue(choice, "text"), stringValue(choice, "content"));
    }

    private static String textFromMessage(Map<String, Object> message) {
        if (message == null) {
            return null;
        }
        String text = firstNonNull(stringValue(message, "text"), stringValue(message, "content"));
        if (text != null) {
            return text;
        }
        List<Object> content = listValue(message, "content");
        if (CollectionUtils.isEmpty(content)) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (Object value : content) {
            if (value instanceof String stringValue) {
                result.append(stringValue);
            }
            else if (value instanceof Map<?, ?> map) {
                result.append(Objects.requireNonNullElse(stringValue(asStringMap(map), "text"), ""));
            }
        }
        return result.toString();
    }

    private static List<AssistantMessage.ToolCall> toolCallsFromMessage(Map<String, Object> message) {
        if (message == null) {
            return Collections.emptyList();
        }
        List<Object> toolCalls = listValue(message, "toolCalls", "tool_calls");
        if (CollectionUtils.isEmpty(toolCalls)) {
            return Collections.emptyList();
        }
        List<AssistantMessage.ToolCall> result = new ArrayList<>();
        for (Object value : toolCalls) {
            if (value instanceof Map<?, ?> map) {
                Map<String, Object> toolCall = asStringMap(map);
                Map<String, Object> function = mapValue(toolCall, "function");
                String name = firstNonNull(stringValue(toolCall, "name"), stringValue(function, "name"));
                if (!StringUtils.hasText(name)) {
                    continue;
                }
                String type = firstNonNull(stringValue(toolCall, "type"), "function");
                String arguments = firstNonNull(argumentsValue(toolCall.get("arguments")),
                        argumentsValue(function != null ? function.get("arguments") : null));
                result.add(new AssistantMessage.ToolCall(stringValue(toolCall, "id"), type, name,
                        Objects.requireNonNullElse(arguments, "{}")));
            }
        }
        return result;
    }

    private static String argumentsValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String stringValue) {
            return stringValue;
        }
        return ModelOptionsUtils.toJsonString(value);
    }

    private static DefaultUsage toUsage(Map<String, Object> usage) {
        if (usage == null) {
            return new DefaultUsage(0, 0, 0);
        }
        Integer promptTokens = integerValue(usage, "promptTokens", "prompt_tokens");
        Integer completionTokens = integerValue(usage, "completionTokens", "completion_tokens");
        Integer totalTokens = integerValue(usage, "totalTokens", "total_tokens");
        return new DefaultUsage(Objects.requireNonNullElse(promptTokens, 0),
                Objects.requireNonNullElse(completionTokens, 0), Objects.requireNonNullElse(totalTokens, 0), usage);
    }

    @SafeVarargs
    private static Map<String, Object> firstMap(Map<String, Object>... values) {
        for (Map<String, Object> value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    @SafeVarargs
    private static List<AssistantMessage.ToolCall> firstNonEmpty(List<AssistantMessage.ToolCall>... values) {
        for (List<AssistantMessage.ToolCall> value : values) {
            if (!CollectionUtils.isEmpty(value)) {
                return value;
            }
        }
        return Collections.emptyList();
    }

    @SafeVarargs
    private static <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static Map<String, Object> mapValue(Map<String, Object> source, String... names) {
        if (source == null) {
            return null;
        }
        for (String name : names) {
            Object value = source.get(name);
            if (value instanceof Map<?, ?> map) {
                return asStringMap(map);
            }
        }
        return null;
    }

    private static List<Object> listValue(Map<String, Object> source, String... names) {
        if (source == null) {
            return Collections.emptyList();
        }
        for (String name : names) {
            Object value = source.get(name);
            if (value instanceof List<?> list) {
                return List.copyOf(list);
            }
        }
        return Collections.emptyList();
    }

    private static Map<String, Object> lastMap(List<Object> values) {
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }
        Object value = values.get(values.size() - 1);
        if (value instanceof Map<?, ?> map) {
            return asStringMap(map);
        }
        return null;
    }

    private static String stringValue(Map<String, Object> source, String... names) {
        if (source == null) {
            return null;
        }
        for (String name : names) {
            Object value = source.get(name);
            if (value instanceof String stringValue) {
                return stringValue;
            }
            if (value instanceof Map<?, ?> map) {
                String nested = stringValue(asStringMap(map), "value");
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private static Integer integerValue(Map<String, Object> source, String... names) {
        if (source == null) {
            return null;
        }
        for (String name : names) {
            Object value = source.get(name);
            if (value instanceof Number number) {
                return number.intValue();
            }
            if (value instanceof String stringValue && StringUtils.hasText(stringValue)) {
                return Integer.valueOf(stringValue);
            }
        }
        return null;
    }

    private static Map<String, Object> asStringMap(Map<?, ?> map) {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() != null) {
                result.put(entry.getKey().toString(), entry.getValue());
            }
        }
        return result;
    }

    private static void closeQuietly(InputStream inputStream) {
        try {
            inputStream.close();
        }
        catch (IOException ignored) {
        }
    }

    private record ExtractedChunk(String id, String model, String modelVersion, String text, String finishReason,
            DefaultUsage usage, List<AssistantMessage.ToolCall> toolCalls) {
        private ExtractedChunk {
            toolCalls = Objects.requireNonNullElse(toolCalls, Collections.emptyList());
        }
    }

}
