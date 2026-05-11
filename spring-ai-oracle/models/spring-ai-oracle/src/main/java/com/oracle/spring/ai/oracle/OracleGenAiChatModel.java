/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import com.oracle.bmc.generativeaiinference.GenerativeAiInference;
import com.oracle.bmc.generativeaiinference.model.BaseChatRequest;
import com.oracle.bmc.generativeaiinference.model.BaseChatResponse;
import com.oracle.bmc.generativeaiinference.model.ChatChoice;
import com.oracle.bmc.generativeaiinference.model.ChatContent;
import com.oracle.bmc.generativeaiinference.model.ChatDetails;
import com.oracle.bmc.generativeaiinference.model.ChatResult;
import com.oracle.bmc.generativeaiinference.model.CohereAssistantMessageV2;
import com.oracle.bmc.generativeaiinference.model.CohereChatBotMessage;
import com.oracle.bmc.generativeaiinference.model.CohereChatRequest;
import com.oracle.bmc.generativeaiinference.model.CohereChatRequestV2;
import com.oracle.bmc.generativeaiinference.model.CohereChatResponse;
import com.oracle.bmc.generativeaiinference.model.CohereChatResponseV2;
import com.oracle.bmc.generativeaiinference.model.CohereContentV2;
import com.oracle.bmc.generativeaiinference.model.CohereMessage;
import com.oracle.bmc.generativeaiinference.model.CohereMessageV2;
import com.oracle.bmc.generativeaiinference.model.CohereSystemMessageV2;
import com.oracle.bmc.generativeaiinference.model.CohereTextContentV2;
import com.oracle.bmc.generativeaiinference.model.CohereUserMessage;
import com.oracle.bmc.generativeaiinference.model.CohereUserMessageV2;
import com.oracle.bmc.generativeaiinference.model.DedicatedServingMode;
import com.oracle.bmc.generativeaiinference.model.GenericChatResponse;
import com.oracle.bmc.generativeaiinference.model.OnDemandServingMode;
import com.oracle.bmc.generativeaiinference.model.ServingMode;
import com.oracle.bmc.generativeaiinference.model.TextContent;
import com.oracle.bmc.generativeaiinference.model.Usage;
import com.oracle.bmc.generativeaiinference.requests.ChatRequest;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.MediaContent;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

/**
 * Spring AI chat model backed by OCI Generative AI.
 */
public class OracleGenAiChatModel implements ChatModel {

    private final GenerativeAiInference client;

    private final OracleGenAiChatOptions defaultOptions;

    private final RetryTemplate retryTemplate;

    public OracleGenAiChatModel(GenerativeAiInference client, OracleGenAiChatOptions defaultOptions) {
        this(client, defaultOptions, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public OracleGenAiChatModel(GenerativeAiInference client, OracleGenAiChatOptions defaultOptions,
            RetryTemplate retryTemplate) {
        Assert.notNull(client, "client must not be null");
        Assert.notNull(defaultOptions, "defaultOptions must not be null");
        Assert.notNull(retryTemplate, "retryTemplate must not be null");
        this.client = client;
        this.defaultOptions = defaultOptions.copy();
        this.retryTemplate = retryTemplate;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        Assert.notNull(prompt, "prompt must not be null");
        OracleGenAiChatOptions options = defaultOptions.merge(prompt.getOptions());
        ChatRequest request = toChatRequest(prompt, options);
        com.oracle.bmc.generativeaiinference.responses.ChatResponse response =
                RetryUtils.execute(retryTemplate, () -> client.chat(request));
        return toChatResponse(response);
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return defaultOptions.copy();
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return Flux.error(new UnsupportedOperationException("OCI Generative AI streaming chat is not supported yet."));
    }

    ChatRequest toChatRequest(Prompt prompt, OracleGenAiChatOptions options) {
        validateOptions(options);
        ChatDetails chatDetails = ChatDetails.builder()
                .compartmentId(options.getCompartmentId())
                .servingMode(toServingMode(options))
                .chatRequest(toBaseChatRequest(prompt, options))
                .build();
        return ChatRequest.builder().chatDetails(chatDetails).build();
    }

    BaseChatRequest toBaseChatRequest(Prompt prompt, OracleGenAiChatOptions options) {
        OracleGenAiChatOptions.ApiFormat apiFormat = resolveApiFormat(options);
        return switch (apiFormat) {
            case GENERIC -> toGenericChatRequest(prompt, options);
            case COHERE_V2 -> toCohereChatRequestV2(prompt, options);
            case COHERE -> toCohereChatRequest(prompt, options);
        };
    }

    private BaseChatRequest toGenericChatRequest(Prompt prompt, OracleGenAiChatOptions options) {
        return GenericChatRequestBuilder.from(prompt)
                .temperature(options.getTemperature())
                .topP(options.getTopP())
                .topK(options.getTopK())
                .maxTokens(options.getMaxTokens())
                .frequencyPenalty(options.getFrequencyPenalty())
                .presencePenalty(options.getPresencePenalty())
                .stop(options.getStopSequences())
                .build();
    }

    private BaseChatRequest toCohereChatRequestV2(Prompt prompt, OracleGenAiChatOptions options) {
        return CohereChatRequestV2.builder()
                .messages(toCohereV2Messages(prompt))
                .temperature(options.getTemperature())
                .topP(options.getTopP())
                .topK(options.getTopK())
                .maxTokens(options.getMaxTokens())
                .frequencyPenalty(options.getFrequencyPenalty())
                .presencePenalty(options.getPresencePenalty())
                .stopSequences(options.getStopSequences())
                .build();
    }

    private BaseChatRequest toCohereChatRequest(Prompt prompt, OracleGenAiChatOptions options) {
        List<Message> instructions = prompt.getInstructions();
        if (instructions.isEmpty()) {
            throw new IllegalArgumentException("Prompt must contain at least one message.");
        }
        List<CohereMessage> history = new ArrayList<>();
        StringJoiner systemMessages = new StringJoiner("\n");
        String userMessage = null;
        for (Message message : instructions) {
            assertSupportedMessage(message);
            if (message.getMessageType() == MessageType.SYSTEM) {
                systemMessages.add(message.getText());
            }
            else if (message.getMessageType() == MessageType.ASSISTANT) {
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

    private static List<com.oracle.bmc.generativeaiinference.model.Message> toGenericMessages(Prompt prompt) {
        List<com.oracle.bmc.generativeaiinference.model.Message> messages = new ArrayList<>();
        for (Message instruction : prompt.getInstructions()) {
            assertSupportedMessage(instruction);
            List<ChatContent> content = Collections.singletonList(TextContent.builder().text(instruction.getText()).build());
            if (instruction.getMessageType() == MessageType.SYSTEM) {
                messages.add(com.oracle.bmc.generativeaiinference.model.SystemMessage.builder().content(content).build());
            }
            else if (instruction.getMessageType() == MessageType.ASSISTANT) {
                messages.add(com.oracle.bmc.generativeaiinference.model.AssistantMessage.builder().content(content).build());
            }
            else if (instruction.getMessageType() == MessageType.USER) {
                messages.add(com.oracle.bmc.generativeaiinference.model.UserMessage.builder().content(content).build());
            }
        }
        return messages;
    }

    private static List<CohereMessageV2> toCohereV2Messages(Prompt prompt) {
        List<CohereMessageV2> messages = new ArrayList<>();
        for (Message instruction : prompt.getInstructions()) {
            assertSupportedMessage(instruction);
            List<CohereContentV2> content =
                    Collections.singletonList(CohereTextContentV2.builder().text(instruction.getText()).build());
            if (instruction.getMessageType() == MessageType.SYSTEM) {
                messages.add(CohereSystemMessageV2.builder().content(content).build());
            }
            else if (instruction.getMessageType() == MessageType.ASSISTANT) {
                messages.add(CohereAssistantMessageV2.builder().content(content).build());
            }
            else if (instruction.getMessageType() == MessageType.USER) {
                messages.add(CohereUserMessageV2.builder().content(content).build());
            }
        }
        return messages;
    }

    private ChatResponse toChatResponse(com.oracle.bmc.generativeaiinference.responses.ChatResponse response) {
        ChatResult chatResult = response.getChatResult();
        if (chatResult == null || chatResult.getChatResponse() == null) {
            throw new IllegalStateException("OCI Generative AI chat response did not contain a chat result.");
        }
        BaseChatResponse baseChatResponse = chatResult.getChatResponse();
        ExtractedResponse extracted = extractResponse(baseChatResponse);
        ChatGenerationMetadata generationMetadata = ChatGenerationMetadata.builder()
                .finishReason(extracted.finishReason())
                .build();
        Generation generation = new Generation(new AssistantMessage(extracted.text()), generationMetadata);
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
            return new ExtractedResponse(null, text, choice.getFinishReason(), usage);
        }
        if (response instanceof CohereChatResponseV2 cohereV2Response) {
            return new ExtractedResponse(cohereV2Response.getId(), textFromCohereV2Message(cohereV2Response.getMessage()),
                    cohereV2Response.getFinishReason() != null ? cohereV2Response.getFinishReason().getValue() : null,
                    cohereV2Response.getUsage());
        }
        if (response instanceof CohereChatResponse cohereResponse) {
            return new ExtractedResponse(null, cohereResponse.getText(),
                    cohereResponse.getFinishReason() != null ? cohereResponse.getFinishReason().getValue() : null,
                    cohereResponse.getUsage());
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

    private static void assertSupportedMessage(Message message) {
        if (message instanceof ToolResponseMessage || message.getMessageType() == MessageType.TOOL) {
            throw new UnsupportedOperationException("OCI Generative AI chat tool response messages are not supported yet.");
        }
        if (message instanceof AssistantMessage assistantMessage && assistantMessage.hasToolCalls()) {
            throw new UnsupportedOperationException("OCI Generative AI chat tool calls are not supported yet.");
        }
        if (message instanceof MediaContent mediaContent && !CollectionUtils.isEmpty(mediaContent.getMedia())) {
            throw new UnsupportedOperationException("OCI Generative AI chat media messages are not supported yet.");
        }
        if (!StringUtils.hasText(message.getText())) {
            throw new IllegalArgumentException("Chat message text must not be empty.");
        }
    }

    private static void validateOptions(OracleGenAiChatOptions options) {
        Assert.hasText(options.getCompartmentId(), "OCI Generative AI compartmentId must be configured.");
        if (options.getServingMode() == null) {
            throw new IllegalArgumentException("OCI Generative AI servingMode must be configured.");
        }
        if (options.getServingMode() == OracleGenAiChatOptions.ServingMode.ON_DEMAND
                && !StringUtils.hasText(options.getModel())) {
            throw new IllegalArgumentException("OCI Generative AI on-demand serving mode requires options.model.");
        }
        if (options.getServingMode() == OracleGenAiChatOptions.ServingMode.DEDICATED
                && !StringUtils.hasText(options.getEndpointId())) {
            throw new IllegalArgumentException("OCI Generative AI dedicated serving mode requires options.endpointId.");
        }
    }

    private static ServingMode toServingMode(OracleGenAiChatOptions options) {
        if (options.getServingMode() == OracleGenAiChatOptions.ServingMode.DEDICATED) {
            return DedicatedServingMode.builder().endpointId(options.getEndpointId()).build();
        }
        return OnDemandServingMode.builder().modelId(options.getModel()).build();
    }

    private static OracleGenAiChatOptions.ApiFormat resolveApiFormat(OracleGenAiChatOptions options) {
        if (options.getApiFormat() != null) {
            return options.getApiFormat();
        }
        String model = options.getModel();
        if (StringUtils.hasText(model) && model.startsWith("cohere.")) {
            return OracleGenAiChatOptions.ApiFormat.COHERE_V2;
        }
        return OracleGenAiChatOptions.ApiFormat.GENERIC;
    }

    private record ExtractedResponse(String id, String text, String finishReason, Usage usage) {
        private ExtractedResponse {
            text = Objects.requireNonNullElse(text, "");
        }
    }

    private static final class GenericChatRequestBuilder {
        private final List<com.oracle.bmc.generativeaiinference.model.Message> messages;
        private Double temperature;
        private Double topP;
        private Integer topK;
        private Integer maxTokens;
        private Double frequencyPenalty;
        private Double presencePenalty;
        private List<String> stop;

        private GenericChatRequestBuilder(List<com.oracle.bmc.generativeaiinference.model.Message> messages) {
            this.messages = messages;
        }

        static GenericChatRequestBuilder from(Prompt prompt) {
            return new GenericChatRequestBuilder(toGenericMessages(prompt));
        }

        GenericChatRequestBuilder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        GenericChatRequestBuilder topP(Double topP) {
            this.topP = topP;
            return this;
        }

        GenericChatRequestBuilder topK(Integer topK) {
            this.topK = topK;
            return this;
        }

        GenericChatRequestBuilder maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        GenericChatRequestBuilder frequencyPenalty(Double frequencyPenalty) {
            this.frequencyPenalty = frequencyPenalty;
            return this;
        }

        GenericChatRequestBuilder presencePenalty(Double presencePenalty) {
            this.presencePenalty = presencePenalty;
            return this;
        }

        GenericChatRequestBuilder stop(List<String> stop) {
            this.stop = stop;
            return this;
        }

        BaseChatRequest build() {
            return com.oracle.bmc.generativeaiinference.model.GenericChatRequest.builder()
                    .messages(messages)
                    .temperature(temperature)
                    .topP(topP)
                    .topK(topK)
                    .maxTokens(maxTokens)
                    .frequencyPenalty(frequencyPenalty)
                    .presencePenalty(presencePenalty)
                    .stop(stop)
                    .build();
        }
    }
}
