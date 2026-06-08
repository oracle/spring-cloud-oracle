/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import com.oracle.bmc.generativeaiinference.model.BaseChatRequest;
import com.oracle.bmc.generativeaiinference.model.ChatContent;
import com.oracle.bmc.generativeaiinference.model.ChatDetails;
import com.oracle.bmc.generativeaiinference.model.CohereAssistantMessageV2;
import com.oracle.bmc.generativeaiinference.model.CohereChatBotMessage;
import com.oracle.bmc.generativeaiinference.model.CohereChatRequest;
import com.oracle.bmc.generativeaiinference.model.CohereChatRequestV2;
import com.oracle.bmc.generativeaiinference.model.CohereContentV2;
import com.oracle.bmc.generativeaiinference.model.CohereMessage;
import com.oracle.bmc.generativeaiinference.model.CohereMessageV2;
import com.oracle.bmc.generativeaiinference.model.CohereSystemMessageV2;
import com.oracle.bmc.generativeaiinference.model.CohereTextContentV2;
import com.oracle.bmc.generativeaiinference.model.CohereToolMessageV2;
import com.oracle.bmc.generativeaiinference.model.CohereUserMessage;
import com.oracle.bmc.generativeaiinference.model.CohereUserMessageV2;
import com.oracle.bmc.generativeaiinference.model.GenericChatRequest;
import com.oracle.bmc.generativeaiinference.model.StreamOptions;
import com.oracle.bmc.generativeaiinference.model.TextContent;
import com.oracle.bmc.generativeaiinference.model.ToolMessage;
import com.oracle.bmc.generativeaiinference.requests.ChatRequest;
import com.oracle.spring.ai.oracle.api.GenAiApiFormat;
import com.oracle.spring.ai.oracle.OracleGenAiChatOptions;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.MediaContent;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public final class ChatRequestConverter {

    public ChatRequest toChatRequest(OracleGenAiChatOptions options, BaseChatRequest chatRequest) {
        ChatDetails chatDetails = ChatDetails.builder()
                .compartmentId(options.getCompartmentId())
                .servingMode(options.toServingMode())
                .chatRequest(chatRequest)
                .build();
        return ChatRequest.builder().chatDetails(chatDetails).build();
    }

    public BaseChatRequest toBaseChatRequest(Prompt prompt, OracleGenAiChatOptions options,
            GenAiApiFormat apiFormat, List<ToolDefinition> toolDefinitions, boolean stream) {
        return switch (apiFormat) {
            case GENERIC -> toGenericChatRequest(prompt, options, toolDefinitions, stream);
            case COHERE_V2 -> toCohereChatRequestV2(prompt, options, toolDefinitions, stream);
            case COHERE -> toCohereChatRequest(prompt, options, stream);
        };
    }

    private BaseChatRequest toGenericChatRequest(Prompt prompt, OracleGenAiChatOptions options,
            List<ToolDefinition> toolDefinitions, boolean stream) {
        return GenericChatRequest.builder()
                .messages(toGenericMessages(prompt))
                .isStream(stream ? true : null)
                .streamOptions(streamOptions(stream))
                .temperature(options.getTemperature())
                .topP(options.getTopP())
                .topK(options.getTopK())
                .maxTokens(options.getMaxTokens())
                .frequencyPenalty(options.getFrequencyPenalty())
                .presencePenalty(options.getPresencePenalty())
                .stop(options.getStopSequences())
                .tools(ChatToolConverter.toGenericTools(toolDefinitions))
                .build();
    }

    private BaseChatRequest toCohereChatRequestV2(Prompt prompt, OracleGenAiChatOptions options,
            List<ToolDefinition> toolDefinitions, boolean stream) {
        return CohereChatRequestV2.builder()
                .messages(toCohereV2Messages(prompt))
                .isStream(stream ? true : null)
                .streamOptions(streamOptions(stream))
                .temperature(options.getTemperature())
                .topP(options.getTopP())
                .topK(options.getTopK())
                .maxTokens(options.getMaxTokens())
                .frequencyPenalty(options.getFrequencyPenalty())
                .presencePenalty(options.getPresencePenalty())
                .stopSequences(options.getStopSequences())
                .tools(ChatToolConverter.toCohereV2Tools(toolDefinitions))
                .build();
    }

    private BaseChatRequest toCohereChatRequest(Prompt prompt, OracleGenAiChatOptions options, boolean stream) {
        assertLegacyCohereOptionsDoNotUseTools(options);
        List<Message> instructions = prompt.getInstructions();
        if (instructions.isEmpty()) {
            throw new IllegalArgumentException("Prompt must contain at least one message.");
        }
        List<CohereMessage> history = new ArrayList<>();
        StringJoiner systemMessages = new StringJoiner("\n");
        String userMessage = null;
        for (Message message : instructions) {
            assertLegacyCohereMessage(message);
            if (message.getMessageType() == MessageType.SYSTEM) {
                systemMessages.add(message.getText());
            }
            else if (message.getMessageType() == MessageType.ASSISTANT) {
                if (userMessage != null) {
                    history.add(CohereUserMessage.builder().message(userMessage).build());
                    userMessage = null;
                }
                history.add(CohereChatBotMessage.builder().message(message.getText()).build());
            }
            else if (message.getMessageType() == MessageType.USER) {
                if (userMessage != null) {
                    history.add(CohereUserMessage.builder().message(userMessage).build());
                }
                userMessage = message.getText();
            }
        }
        if (!StringUtils.hasText(userMessage)) {
            throw new IllegalArgumentException("Legacy Cohere chat requests require a user message.");
        }
        CohereChatRequest.Builder builder = CohereChatRequest.builder()
                .message(userMessage)
                .isStream(stream ? true : null)
                .streamOptions(streamOptions(stream))
                .temperature(options.getTemperature())
                .topP(options.getTopP())
                .topK(options.getTopK())
                .maxTokens(options.getMaxTokens())
                .frequencyPenalty(options.getFrequencyPenalty())
                .presencePenalty(options.getPresencePenalty())
                .stopSequences(options.getStopSequences());
        if (!history.isEmpty()) {
            builder.chatHistory(history);
        }
        String preambleOverride = systemMessages.toString();
        if (StringUtils.hasText(preambleOverride)) {
            builder.preambleOverride(preambleOverride);
        }
        return builder.build();
    }

    private static StreamOptions streamOptions(boolean stream) {
        if (!stream) {
            return null;
        }
        return StreamOptions.builder().isIncludeUsage(true).build();
    }

    private static List<com.oracle.bmc.generativeaiinference.model.Message> toGenericMessages(Prompt prompt) {
        List<com.oracle.bmc.generativeaiinference.model.Message> messages = new ArrayList<>();
        for (Message instruction : prompt.getInstructions()) {
            assertGenericOrCohereV2Message(instruction);
            if (instruction instanceof ToolResponseMessage toolResponseMessage) {
                messages.addAll(toGenericToolMessages(toolResponseMessage));
            }
            else if (instruction.getMessageType() == MessageType.SYSTEM) {
                messages.add(com.oracle.bmc.generativeaiinference.model.SystemMessage.builder()
                        .content(genericContent(instruction.getText()))
                        .build());
            }
            else if (instruction.getMessageType() == MessageType.ASSISTANT) {
                AssistantMessage assistantMessage = (AssistantMessage) instruction;
                messages.add(com.oracle.bmc.generativeaiinference.model.AssistantMessage.builder()
                        .content(genericContentIfPresent(assistantMessage.getText()))
                        .toolCalls(ChatToolConverter.toGenericToolCalls(assistantMessage.getToolCalls()))
                        .build());
            }
            else if (instruction.getMessageType() == MessageType.USER) {
                messages.add(com.oracle.bmc.generativeaiinference.model.UserMessage.builder()
                        .content(genericContent(instruction.getText()))
                        .build());
            }
        }
        return messages;
    }

    private static List<ToolMessage> toGenericToolMessages(ToolResponseMessage message) {
        return message.getResponses()
                .stream()
                .map(response -> ToolMessage.builder()
                        .toolCallId(response.id())
                        .content(genericContent(response.responseData()))
                        .build())
                .toList();
    }

    private static List<CohereMessageV2> toCohereV2Messages(Prompt prompt) {
        List<CohereMessageV2> messages = new ArrayList<>();
        for (Message instruction : prompt.getInstructions()) {
            assertGenericOrCohereV2Message(instruction);
            if (instruction instanceof ToolResponseMessage toolResponseMessage) {
                messages.addAll(toCohereV2ToolMessages(toolResponseMessage));
            }
            else if (instruction.getMessageType() == MessageType.SYSTEM) {
                messages.add(CohereSystemMessageV2.builder()
                        .content(cohereV2Content(instruction.getText()))
                        .build());
            }
            else if (instruction.getMessageType() == MessageType.ASSISTANT) {
                AssistantMessage assistantMessage = (AssistantMessage) instruction;
                messages.add(CohereAssistantMessageV2.builder()
                        .content(cohereV2ContentIfPresent(assistantMessage.getText()))
                        .toolCalls(ChatToolConverter.toCohereV2ToolCalls(assistantMessage.getToolCalls()))
                        .build());
            }
            else if (instruction.getMessageType() == MessageType.USER) {
                messages.add(CohereUserMessageV2.builder()
                        .content(cohereV2Content(instruction.getText()))
                        .build());
            }
        }
        return messages;
    }

    private static List<CohereToolMessageV2> toCohereV2ToolMessages(ToolResponseMessage message) {
        return message.getResponses()
                .stream()
                .map(response -> CohereToolMessageV2.builder()
                        .toolCallId(response.id())
                        .content(cohereV2Content(response.responseData()))
                        .build())
                .toList();
    }

    private static void assertGenericOrCohereV2Message(Message message) {
        assertNoMedia(message);
        if (message instanceof ToolResponseMessage) {
            return;
        }
        if (message.getMessageType() == MessageType.TOOL) {
            throw new IllegalArgumentException("Unsupported tool message type: " + message.getClass().getName());
        }
        if (message.getMessageType() == MessageType.ASSISTANT && message instanceof AssistantMessage assistantMessage
                && assistantMessage.hasToolCalls()) {
            assistantMessage.getToolCalls().forEach(ChatToolConverter::assertFunctionToolCall);
            return;
        }
        if (!StringUtils.hasText(message.getText())) {
            throw new IllegalArgumentException("Chat message text must not be empty.");
        }
    }

    private static void assertLegacyCohereMessage(Message message) {
        if (message instanceof ToolResponseMessage || message.getMessageType() == MessageType.TOOL) {
            throw ChatToolConverter.legacyCohereToolCallingUnsupported();
        }
        if (message instanceof AssistantMessage assistantMessage && assistantMessage.hasToolCalls()) {
            throw ChatToolConverter.legacyCohereToolCallingUnsupported();
        }
        assertNoMedia(message);
        if (!StringUtils.hasText(message.getText())) {
            throw new IllegalArgumentException("Chat message text must not be empty.");
        }
    }

    private static void assertNoMedia(Message message) {
        if (message instanceof MediaContent mediaContent && !CollectionUtils.isEmpty(mediaContent.getMedia())) {
            throw new UnsupportedOperationException("OCI Generative AI chat media messages are not supported yet.");
        }
    }

    private static void assertLegacyCohereOptionsDoNotUseTools(OracleGenAiChatOptions options) {
        if (!CollectionUtils.isEmpty(options.getToolCallbacks())) {
            throw ChatToolConverter.legacyCohereToolCallingUnsupported();
        }
    }

    private static List<ChatContent> genericContent(String text) {
        return Collections.singletonList(TextContent.builder().text(Objects.requireNonNullElse(text, "")).build());
    }

    private static List<ChatContent> genericContentIfPresent(String text) {
        if (!StringUtils.hasText(text)) {
            return Collections.emptyList();
        }
        return genericContent(text);
    }

    private static List<CohereContentV2> cohereV2Content(String text) {
        return Collections.singletonList(CohereTextContentV2.builder()
                .text(Objects.requireNonNullElse(text, ""))
                .build());
    }

    private static List<CohereContentV2> cohereV2ContentIfPresent(String text) {
        if (!StringUtils.hasText(text)) {
            return Collections.emptyList();
        }
        return cohereV2Content(text);
    }

}
