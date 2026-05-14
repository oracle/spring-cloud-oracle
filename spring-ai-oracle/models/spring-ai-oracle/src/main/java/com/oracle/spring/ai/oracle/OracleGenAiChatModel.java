/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.oracle.bmc.generativeaiinference.GenerativeAiInference;
import com.oracle.bmc.generativeaiinference.model.BaseChatRequest;
import com.oracle.bmc.generativeaiinference.requests.ChatRequest;
import com.oracle.spring.ai.oracle.api.GenAiApiFormat;
import com.oracle.spring.ai.oracle.converter.ChatRequestConverter;
import com.oracle.spring.ai.oracle.converter.ChatResponseConverter;
import com.oracle.spring.ai.oracle.converter.ChatStreamResponseConverter;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.MessageAggregator;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.DefaultToolExecutionEligibilityPredicate;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionEligibilityPredicate;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.model.tool.internal.ToolCallReactiveContextHolder;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.support.UsageCalculator;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static com.oracle.spring.ai.oracle.api.GenAiApiFormat.infer;

/**
 * Spring AI chat model backed by OCI Generative AI.
 */
public class OracleGenAiChatModel implements ChatModel {

    private static final ToolCallingManager DEFAULT_TOOL_CALLING_MANAGER = ToolCallingManager.builder().build();

    private static final ToolExecutionEligibilityPredicate DEFAULT_TOOL_EXECUTION_ELIGIBILITY_PREDICATE =
            new DefaultToolExecutionEligibilityPredicate();

    private final GenerativeAiInference client;

    private final OracleGenAiChatOptions defaultOptions;

    private final ToolCallingManager toolCallingManager;

    private final ToolExecutionEligibilityPredicate toolExecutionEligibilityPredicate;

    private final RetryTemplate retryTemplate;

    private final ChatRequestConverter requestConverter = new ChatRequestConverter();

    private final ChatResponseConverter responseConverter = new ChatResponseConverter();

    private final ChatStreamResponseConverter streamResponseConverter = new ChatStreamResponseConverter();

    public static Builder builder() {
        return new Builder();
    }

    public OracleGenAiChatModel(GenerativeAiInference client, OracleGenAiChatOptions defaultOptions) {
        this(client, defaultOptions, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public OracleGenAiChatModel(GenerativeAiInference client, OracleGenAiChatOptions defaultOptions,
            RetryTemplate retryTemplate) {
        this(client, defaultOptions, DEFAULT_TOOL_CALLING_MANAGER, retryTemplate);
    }

    public OracleGenAiChatModel(GenerativeAiInference client, OracleGenAiChatOptions defaultOptions,
            ToolCallingManager toolCallingManager, RetryTemplate retryTemplate) {
        this(client, defaultOptions, toolCallingManager, DEFAULT_TOOL_EXECUTION_ELIGIBILITY_PREDICATE, retryTemplate);
    }

    public OracleGenAiChatModel(GenerativeAiInference client, OracleGenAiChatOptions defaultOptions,
            ToolCallingManager toolCallingManager, ToolExecutionEligibilityPredicate toolExecutionEligibilityPredicate,
            RetryTemplate retryTemplate) {
        Assert.notNull(client, "client must not be null");
        Assert.notNull(defaultOptions, "defaultOptions must not be null");
        Assert.notNull(toolCallingManager, "toolCallingManager must not be null");
        Assert.notNull(toolExecutionEligibilityPredicate, "toolExecutionEligibilityPredicate must not be null");
        Assert.notNull(retryTemplate, "retryTemplate must not be null");
        this.client = client;
        this.defaultOptions = defaultOptions.copy();
        this.toolCallingManager = toolCallingManager;
        this.toolExecutionEligibilityPredicate = toolExecutionEligibilityPredicate;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        Assert.notNull(prompt, "prompt must not be null");
        return internalCall(new Prompt(prompt.getInstructions(), defaultOptions.merge(prompt.getOptions())), null);
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return defaultOptions.copy();
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        Assert.notNull(prompt, "prompt must not be null");
        return internalStream(new Prompt(prompt.getInstructions(), defaultOptions.merge(prompt.getOptions())), null);
    }

    public Builder mutate() {
        return builder()
                .client(client)
                .defaultOptions(defaultOptions)
                .toolCallingManager(toolCallingManager)
                .toolExecutionEligibilityPredicate(toolExecutionEligibilityPredicate)
                .retryTemplate(retryTemplate);
    }

    BaseChatRequest toBaseChatRequest(Prompt prompt, OracleGenAiChatOptions options) {
        GenAiApiFormat apiFormat = options.getApiFormat() != null ? options.getApiFormat() : infer(options.getModel());
        List<ToolDefinition> toolDefinitions = resolveToolDefinitions(options, apiFormat);
        return requestConverter.toBaseChatRequest(prompt, options, apiFormat, toolDefinitions, false);
    }

    private ChatResponse internalCall(Prompt prompt, ChatResponse previousChatResponse) {
        OracleGenAiChatOptions options = (OracleGenAiChatOptions) prompt.getOptions();
        ChatRequest request = toChatRequest(prompt, options, false);
        com.oracle.bmc.generativeaiinference.responses.ChatResponse response =
                RetryUtils.execute(retryTemplate, () -> client.chat(request));
        ChatResponse chatResponse = withCumulativeUsage(responseConverter.toChatResponse(response), previousChatResponse);
        if (toolExecutionEligibilityPredicate.isToolExecutionRequired(options, chatResponse)) {
            ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, chatResponse);
            if (toolExecutionResult.returnDirect()) {
                return ChatResponse.builder()
                        .from(chatResponse)
                        .generations(ToolExecutionResult.buildGenerations(toolExecutionResult))
                        .build();
            }
            return internalCall(new Prompt(toolExecutionResult.conversationHistory(), options), chatResponse);
        }
        return chatResponse;
    }

    private Flux<ChatResponse> internalStream(Prompt prompt, ChatResponse previousChatResponse) {
        OracleGenAiChatOptions options = (OracleGenAiChatOptions) prompt.getOptions();
        ChatRequest request = toChatRequest(prompt, options, true);
        Flux<ChatResponse> chatResponseFlux = Flux.defer(() -> {
            com.oracle.bmc.generativeaiinference.responses.ChatResponse response =
                    RetryUtils.execute(retryTemplate, () -> client.chat(request));
            return streamResponseConverter.toChatResponses(response);
        })
                .map(chatResponse -> withStreamingMetadataDefaults(chatResponse, options))
                .map(chatResponse -> withCumulativeUsage(chatResponse, previousChatResponse))
                .subscribeOn(Schedulers.boundedElastic());

        if (!isInternalToolExecutionPossible(options)) {
            return chatResponseFlux;
        }

        return Flux.deferContextual(contextView -> {
            List<ChatResponse> streamedChatResponses = new ArrayList<>();
            ChatResponse[] aggregatedChatResponse = new ChatResponse[1];
            return new MessageAggregator()
                    .aggregate(chatResponseFlux.doOnNext(streamedChatResponses::add),
                            chatResponse -> aggregatedChatResponse[0] = chatResponse)
                    .thenMany(Flux.defer(() -> {
                        ChatResponse chatResponse = aggregatedChatResponse[0];
                        if (chatResponse == null
                                || !toolExecutionEligibilityPredicate.isToolExecutionRequired(options, chatResponse)) {
                            return Flux.fromIterable(streamedChatResponses);
                        }
                        return Mono.fromCallable(() -> {
                            ToolCallReactiveContextHolder.setContext(contextView);
                            try {
                                return toolCallingManager.executeToolCalls(prompt, chatResponse);
                            }
                            finally {
                                ToolCallReactiveContextHolder.clearContext();
                            }
                        })
                                .subscribeOn(Schedulers.boundedElastic())
                                .flatMapMany(toolExecutionResult -> {
                                    if (toolExecutionResult.returnDirect()) {
                                        return Flux.just(ChatResponse.builder()
                                                .from(chatResponse)
                                                .generations(ToolExecutionResult.buildGenerations(toolExecutionResult))
                                                .build());
                                    }
                                    return internalStream(new Prompt(toolExecutionResult.conversationHistory(), options),
                                            chatResponse);
                                });
                    }));
        });
    }

    private ChatRequest toChatRequest(Prompt prompt, OracleGenAiChatOptions options, boolean stream) {
        options.validate();
        GenAiApiFormat apiFormat = options.getApiFormat() != null ? options.getApiFormat() : infer(options.getModel());
        List<ToolDefinition> toolDefinitions = resolveToolDefinitions(options, apiFormat);
        BaseChatRequest baseChatRequest =
                requestConverter.toBaseChatRequest(prompt, options, apiFormat, toolDefinitions, stream);
        return requestConverter.toChatRequest(options, baseChatRequest);
    }

    private static ChatResponse withStreamingMetadataDefaults(ChatResponse chatResponse,
            OracleGenAiChatOptions options) {
        if (chatResponse.getMetadata().getModel() != null) {
            return chatResponse;
        }
        ChatResponseMetadata.Builder metadata = ChatResponseMetadata.builder()
                .id(chatResponse.getMetadata().getId())
                .model(options.getModel())
                .rateLimit(chatResponse.getMetadata().getRateLimit())
                .usage(chatResponse.getMetadata().getUsage())
                .promptMetadata(chatResponse.getMetadata().getPromptMetadata());
        chatResponse.getMetadata().entrySet().forEach(entry -> metadata.keyValue(entry.getKey(), entry.getValue()));
        return ChatResponse.builder().from(chatResponse).metadata(metadata.build()).build();
    }

    private static ChatResponse withCumulativeUsage(ChatResponse chatResponse, ChatResponse previousChatResponse) {
        Usage usage = UsageCalculator.getCumulativeUsage(chatResponse.getMetadata().getUsage(), previousChatResponse);
        if (usage == chatResponse.getMetadata().getUsage()) {
            return chatResponse;
        }
        ChatResponseMetadata.Builder metadata = ChatResponseMetadata.builder()
                .id(chatResponse.getMetadata().getId())
                .model(chatResponse.getMetadata().getModel())
                .rateLimit(chatResponse.getMetadata().getRateLimit())
                .usage(usage)
                .promptMetadata(chatResponse.getMetadata().getPromptMetadata());
        chatResponse.getMetadata().entrySet().forEach(entry -> metadata.keyValue(entry.getKey(), entry.getValue()));
        return ChatResponse.builder().from(chatResponse).metadata(metadata.build()).build();
    }

    private List<ToolDefinition> resolveToolDefinitions(OracleGenAiChatOptions options,
            GenAiApiFormat apiFormat) {
        if (apiFormat == GenAiApiFormat.COHERE) {
            return Collections.emptyList();
        }
        ToolCallingChatOptions.validateToolCallbacks(options.getToolCallbacks());
        return toolCallingManager.resolveToolDefinitions(options);
    }

    private static boolean isInternalToolExecutionPossible(OracleGenAiChatOptions options) {
        return ToolCallingChatOptions.isInternalToolExecutionEnabled(options)
                && (!CollectionUtils.isEmpty(options.getToolCallbacks())
                || !CollectionUtils.isEmpty(options.getToolNames()));
    }

    public static final class Builder {

        private GenerativeAiInference client;

        private OracleGenAiChatOptions defaultOptions;

        private ToolCallingManager toolCallingManager = DEFAULT_TOOL_CALLING_MANAGER;

        private ToolExecutionEligibilityPredicate toolExecutionEligibilityPredicate =
                DEFAULT_TOOL_EXECUTION_ELIGIBILITY_PREDICATE;

        private RetryTemplate retryTemplate = RetryUtils.DEFAULT_RETRY_TEMPLATE;

        private Builder() {
        }

        public Builder client(GenerativeAiInference client) {
            this.client = client;
            return this;
        }

        public Builder defaultOptions(OracleGenAiChatOptions defaultOptions) {
            this.defaultOptions = defaultOptions;
            return this;
        }

        public Builder toolCallingManager(ToolCallingManager toolCallingManager) {
            this.toolCallingManager = toolCallingManager;
            return this;
        }

        public Builder toolExecutionEligibilityPredicate(
                ToolExecutionEligibilityPredicate toolExecutionEligibilityPredicate) {
            this.toolExecutionEligibilityPredicate = toolExecutionEligibilityPredicate;
            return this;
        }

        public Builder retryTemplate(RetryTemplate retryTemplate) {
            this.retryTemplate = retryTemplate;
            return this;
        }

        public OracleGenAiChatModel build() {
            return new OracleGenAiChatModel(client, defaultOptions, toolCallingManager,
                    toolExecutionEligibilityPredicate, retryTemplate);
        }
    }

}
