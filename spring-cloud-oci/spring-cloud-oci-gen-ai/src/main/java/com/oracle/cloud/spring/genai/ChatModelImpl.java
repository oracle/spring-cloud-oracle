/*
 ** Copyright (c) 2024, 2025, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.genai;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.oracle.bmc.generativeaiinference.GenerativeAiInference;
import com.oracle.bmc.generativeaiinference.model.BaseChatRequest;
import com.oracle.bmc.generativeaiinference.model.ChatContent;
import com.oracle.bmc.generativeaiinference.model.ChatDetails;
import com.oracle.bmc.generativeaiinference.model.CohereChatRequest;
import com.oracle.bmc.generativeaiinference.model.GenericChatRequest;
import com.oracle.bmc.generativeaiinference.model.Message;
import com.oracle.bmc.generativeaiinference.model.ServingMode;
import com.oracle.bmc.generativeaiinference.model.TextContent;
import com.oracle.bmc.generativeaiinference.model.UserMessage;
import com.oracle.bmc.generativeaiinference.requests.ChatRequest;
import com.oracle.bmc.generativeaiinference.responses.ChatResponse;
import org.springframework.util.Assert;

/**
 * OCI GenAI Chat
 * @deprecated in favor of Spring AI. This implementation will be replaced by Spring AI integration.
 */
@Deprecated(since = "2.0.1", forRemoval = false)
public class ChatModelImpl implements ChatModel {
    private final GenerativeAiInference client;
    private final ServingMode servingMode;
    private final String compartment;
    private final String preambleOverride;
    private final Double temperature;
    private final Double frequencyPenalty;
    private final Integer maxTokens;
    private final Double presencePenalty;
    private final Double topP;
    private final Integer topK;
    private final InferenceRequestType inferenceRequestType;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private GenerativeAiInference client;
        private ServingMode servingMode;
        private String compartment;
        private String preambleOverride;
        private Double temperature;
        private Double frequencyPenalty;
        private Integer maxTokens;
        private Double presencePenalty;
        private Double topP;
        private Integer topK;
        private InferenceRequestType inferenceRequestType;

        public Builder client(GenerativeAiInference client) {
            this.client = client;
            return this;
        }

        public Builder servingMode(ServingMode servingMode) {
            this.servingMode = servingMode;
            return this;
        }

        public Builder compartment(String compartment) {
            this.compartment = compartment;
            return this;
        }

        public Builder preambleOverride(String preambleOverride) {
            this.preambleOverride = preambleOverride;
            return this;
        }

        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder frequencyPenalty(Double frequencyPenalty) {
            this.frequencyPenalty = frequencyPenalty;
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder presencePenalty(Double presencePenalty) {
            this.presencePenalty = presencePenalty;
            return this;
        }

        public Builder topP(Double topP) {
            this.topP = topP;
            return this;
        }

        public Builder topK(Integer topK) {
            this.topK = topK;
            return this;
        }

        public Builder inferenceRequestType(InferenceRequestType inferenceRequestType) {
            this.inferenceRequestType = inferenceRequestType;
            return this;
        }

        public ChatModelImpl build() {
            return new ChatModelImpl(this);
        }
    }

    public ChatModelImpl(Builder builder) {
        this(builder.client,
                builder.servingMode,
                builder.compartment,
                builder.preambleOverride,
                builder.temperature,
                builder.frequencyPenalty,
                builder.maxTokens,
                builder.presencePenalty,
                builder.topP,
                builder.topK,
                builder.inferenceRequestType);
    }

    public ChatModelImpl(GenerativeAiInference client,
                         ServingMode servingMode,
                         String compartment,
                         String preambleOverride,
                         Double temperature,
                         Double frequencyPenalty,
                         Integer maxTokens,
                         Double presencePenalty,
                         Double topP,
                         Integer topK,
                         InferenceRequestType inferenceRequestType) {
        Assert.notNull(client, "client must not be null");
        Assert.notNull(servingMode, "servingMode must not be null");
        Assert.hasText(compartment, "compartment must be present");
        this.client = client;
        this.servingMode = servingMode;
        this.compartment = compartment;
        this.preambleOverride = preambleOverride;

        this.temperature = Optional.ofNullable(temperature).orElse(1.0);
        this.frequencyPenalty = Optional.ofNullable(frequencyPenalty).orElse(0.0);
        this.maxTokens = Optional.ofNullable(maxTokens).orElse(600);
        this.presencePenalty = Optional.ofNullable(presencePenalty).orElse(0.0);
        this.topP = Optional.ofNullable(topP).orElse(0.75);
        this.inferenceRequestType = Optional.ofNullable(inferenceRequestType).orElse(InferenceRequestType.COHERE);
        this.topK = Optional.ofNullable(topK).orElseGet(() -> {
            if (this.inferenceRequestType == InferenceRequestType.COHERE) {
                return 0;
            }
            return -1;
        });
    }



    /**
     * Chat using OCI GenAI.
     * @param prompt Prompt text sent to OCI GenAI chat model.
     * @return OCI GenAI ChatResponse
     */
    public ChatResponse chat(String prompt) {
        ChatDetails chatDetails = ChatDetails.builder()
                .compartmentId(compartment)
                .servingMode(servingMode)
                .chatRequest(createChatRequest(prompt))
                .build();
        ChatRequest chatRequest = ChatRequest.builder()
                .body$(chatDetails)
                .build();
        return client.chat(chatRequest);
    }

    /**
     * Create a ChatRequest from a text prompt. Supports COHERE or LLAMA inference types.
     * @param prompt To create a ChatRequest from.
     * @return A COHERE or LLAMA ChatRequest.
     */
    private BaseChatRequest createChatRequest(String prompt) {
        switch (inferenceRequestType) {
            case COHERE:
                return CohereChatRequest.builder()
                    .frequencyPenalty(frequencyPenalty)
                    .maxTokens(maxTokens)
                    .presencePenalty(presencePenalty)
                    .message(prompt)
                    .temperature(temperature)
                    .topP(topP)
                    .topK(topK)
                    .preambleOverride(preambleOverride)
                    .build();
            case LLAMA:
                ChatContent content = TextContent.builder()
                        .text(prompt)
                        .build();
                List<ChatContent> contents = new ArrayList<>();
                contents.add(content);
                UserMessage message = UserMessage.builder()
                        .name("USER")
                        .content(contents)
                        .build();
                List<Message> messages = new ArrayList<>();
                messages.add(message);
                return GenericChatRequest.builder()
                        .messages(messages)
                        .frequencyPenalty(frequencyPenalty)
                        .temperature(temperature)
                        .maxTokens(maxTokens)
                        .presencePenalty(presencePenalty)
                        .topP(topP)
                        .topK(topK)
                        .build();
        }
        throw new IllegalArgumentException("Unsupported inference request type: " + inferenceRequestType);
    }
}
