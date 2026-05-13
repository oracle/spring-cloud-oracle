/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle.converter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.oracle.bmc.generativeaiinference.model.BaseChatResponse;
import com.oracle.bmc.generativeaiinference.model.ChatChoice;
import com.oracle.bmc.generativeaiinference.model.ChatContent;
import com.oracle.bmc.generativeaiinference.model.ChatResult;
import com.oracle.bmc.generativeaiinference.model.CohereAssistantMessageV2;
import com.oracle.bmc.generativeaiinference.model.CohereChatResponse;
import com.oracle.bmc.generativeaiinference.model.CohereChatResponseV2;
import com.oracle.bmc.generativeaiinference.model.CohereContentV2;
import com.oracle.bmc.generativeaiinference.model.CohereTextContentV2;
import com.oracle.bmc.generativeaiinference.model.GenericChatResponse;
import com.oracle.bmc.generativeaiinference.model.TextContent;
import com.oracle.bmc.generativeaiinference.model.Usage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.util.CollectionUtils;

public final class ChatResponseConverter {

    public ChatResponse toChatResponse(com.oracle.bmc.generativeaiinference.responses.ChatResponse response) {
        ChatResult chatResult = response.getChatResult();
        if (chatResult == null || chatResult.getChatResponse() == null) {
            throw new IllegalStateException("OCI Generative AI chat response did not contain a chat result.");
        }
        BaseChatResponse baseChatResponse = chatResult.getChatResponse();
        ExtractedResponse extracted = extractResponse(baseChatResponse);
        ChatGenerationMetadata generationMetadata = ChatGenerationMetadata.builder()
                .finishReason(extracted.finishReason())
                .build();
        AssistantMessage assistantMessage = AssistantMessage.builder()
                .content(extracted.text())
                .toolCalls(extracted.toolCalls())
                .build();
        Generation generation = new Generation(assistantMessage, generationMetadata);
        ChatResponseMetadata metadata = ChatResponseMetadata.builder()
                .id(extracted.id())
                .model(chatResult.getModelId())
                .usage(toUsage(extracted.usage()))
                .keyValue("modelVersion", chatResult.getModelVersion())
                .keyValue("opcRequestId", response.getOpcRequestId())
                .build();
        return new ChatResponse(Collections.singletonList(generation), metadata);
    }

    private static ExtractedResponse extractResponse(BaseChatResponse response) {
        if (response instanceof GenericChatResponse genericResponse) {
            ChatChoice choice = last(genericResponse.getChoices(), "generic chat choices");
            String text = textFromGenericMessage(choice.getMessage());
            Usage usage = choice.getUsage() != null ? choice.getUsage() : genericResponse.getUsage();
            return new ExtractedResponse(null, text, choice.getFinishReason(), usage,
                    toolCallsFromGenericMessage(choice.getMessage()));
        }
        if (response instanceof CohereChatResponseV2 cohereV2Response) {
            return new ExtractedResponse(cohereV2Response.getId(), textFromCohereV2Message(cohereV2Response.getMessage()),
                    cohereV2Response.getFinishReason() != null ? cohereV2Response.getFinishReason().getValue() : null,
                    cohereV2Response.getUsage(), toolCallsFromCohereV2Message(cohereV2Response.getMessage()));
        }
        if (response instanceof CohereChatResponse cohereResponse) {
            if (!CollectionUtils.isEmpty(cohereResponse.getToolCalls())) {
                throw ChatToolConverter.legacyCohereToolCallingUnsupported();
            }
            return new ExtractedResponse(null, cohereResponse.getText(),
                    cohereResponse.getFinishReason() != null ? cohereResponse.getFinishReason().getValue() : null,
                    cohereResponse.getUsage(), Collections.emptyList());
        }
        throw new IllegalStateException("Unsupported OCI Generative AI chat response type: " + response.getClass().getName());
    }

    private static String textFromGenericMessage(com.oracle.bmc.generativeaiinference.model.Message message) {
        if (message == null || CollectionUtils.isEmpty(message.getContent())) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        for (ChatContent content : message.getContent()) {
            if (content instanceof TextContent textContent) {
                text.append(textContent.getText());
            }
        }
        return text.toString();
    }

    private static List<AssistantMessage.ToolCall> toolCallsFromGenericMessage(
            com.oracle.bmc.generativeaiinference.model.Message message) {
        if (message instanceof com.oracle.bmc.generativeaiinference.model.AssistantMessage assistantMessage) {
            return ChatToolConverter.fromGenericToolCalls(assistantMessage.getToolCalls());
        }
        return Collections.emptyList();
    }

    private static String textFromCohereV2Message(CohereAssistantMessageV2 message) {
        if (message == null || CollectionUtils.isEmpty(message.getContent())) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        for (CohereContentV2 content : message.getContent()) {
            if (content instanceof CohereTextContentV2 textContent) {
                text.append(textContent.getText());
            }
        }
        return text.toString();
    }

    private static List<AssistantMessage.ToolCall> toolCallsFromCohereV2Message(CohereAssistantMessageV2 message) {
        if (message == null) {
            return Collections.emptyList();
        }
        return ChatToolConverter.fromCohereV2ToolCalls(message.getToolCalls());
    }

    private static DefaultUsage toUsage(Usage usage) {
        if (usage == null) {
            return new DefaultUsage(0, 0, 0);
        }
        return new DefaultUsage(usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens(), usage);
    }

    private static <T> T last(List<T> values, String description) {
        if (CollectionUtils.isEmpty(values)) {
            throw new IllegalStateException("OCI Generative AI response did not contain " + description + ".");
        }
        return values.get(values.size() - 1);
    }

    private record ExtractedResponse(String id, String text, String finishReason, Usage usage,
            List<AssistantMessage.ToolCall> toolCalls) {
        private ExtractedResponse {
            text = Objects.requireNonNullElse(text, "");
            toolCalls = Objects.requireNonNullElse(toolCalls, Collections.emptyList());
        }
    }

}
