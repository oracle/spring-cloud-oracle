/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.oracle.bmc.generativeaiinference.GenerativeAiInference;
import com.oracle.bmc.generativeaiinference.model.BaseChatRequest;
import com.oracle.bmc.generativeaiinference.requests.ChatRequest;
import com.oracle.spring.ai.oracle.api.GenAiApiFormat;
import com.oracle.spring.ai.oracle.converter.ChatRequestConverter;
import com.oracle.spring.ai.oracle.converter.ChatResponseConverter;
import com.oracle.spring.ai.oracle.converter.ChatStreamResponseConverter;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.MessageAggregator;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.chat.observation.ChatModelObservationDocumentation;
import org.springframework.ai.chat.observation.DefaultChatModelObservationConvention;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionEligibilityChecker;
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

    private static final ChatModelObservationConvention DEFAULT_OBSERVATION_CONVENTION =
            new DefaultChatModelObservationConvention();

    private static final ToolCallingManager DEFAULT_TOOL_CALLING_MANAGER = ToolCallingManager.builder().build();

    private static final ToolExecutionEligibilityChecker DEFAULT_TOOL_EXECUTION_ELIGIBILITY_CHECKER =
            ChatResponse::hasToolCalls;

    private final GenerativeAiInference client;

    private final OracleGenAiChatOptions defaultOptions;

    private final ToolCallingManager toolCallingManager;

    private final ToolExecutionEligibilityChecker toolExecutionEligibilityChecker;

    private final RetryTemplate retryTemplate;

    private final ObservationRegistry observationRegistry;

    private ChatModelObservationConvention observationConvention = DEFAULT_OBSERVATION_CONVENTION;

    private final ChatRequestConverter requestConverter = new ChatRequestConverter();

    private final ChatResponseConverter responseConverter = new ChatResponseConverter();

    private final ChatStreamResponseConverter streamResponseConverter = new ChatStreamResponseConverter();

    public static Builder builder() {
        return new Builder();
    }

    private OracleGenAiChatModel(@Nullable GenerativeAiInference client,
            @Nullable OracleGenAiChatOptions defaultOptions, @Nullable ToolCallingManager toolCallingManager,
            @Nullable ToolExecutionEligibilityChecker toolExecutionEligibilityChecker,
            @Nullable RetryTemplate retryTemplate, @Nullable ObservationRegistry observationRegistry,
            @Nullable ChatModelObservationConvention observationConvention) {
        this.client = Objects.requireNonNull(client, "client must not be null");
        this.defaultOptions = Objects.requireNonNull(defaultOptions, "defaultOptions must not be null").copy();
        this.toolCallingManager = Objects.requireNonNullElse(toolCallingManager, DEFAULT_TOOL_CALLING_MANAGER);
        this.toolExecutionEligibilityChecker = Objects.requireNonNullElse(toolExecutionEligibilityChecker,
                DEFAULT_TOOL_EXECUTION_ELIGIBILITY_CHECKER);
        this.retryTemplate = Objects.requireNonNullElse(retryTemplate, RetryUtils.DEFAULT_RETRY_TEMPLATE);
        this.observationRegistry = Objects.requireNonNullElse(observationRegistry, ObservationRegistry.NOOP);
        this.observationConvention = Objects.requireNonNullElse(observationConvention, DEFAULT_OBSERVATION_CONVENTION);
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        Assert.notNull(prompt, "prompt must not be null");
        return internalCall(new Prompt(prompt.getInstructions(), defaultOptions.merge(prompt.getOptions())), null);
    }

    @Override
    public ChatOptions getOptions() {
        return defaultOptions.copy();
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        Assert.notNull(prompt, "prompt must not be null");
        return internalStream(new Prompt(prompt.getInstructions(), defaultOptions.merge(prompt.getOptions())), null);
    }

    public void setObservationConvention(ChatModelObservationConvention observationConvention) {
        Assert.notNull(observationConvention, "observationConvention must not be null");
        this.observationConvention = observationConvention;
    }

    BaseChatRequest toBaseChatRequest(Prompt prompt, OracleGenAiChatOptions options) {
        GenAiApiFormat apiFormat = options.getApiFormat() != null ? options.getApiFormat() : infer(options.getModel());
        List<ToolDefinition> toolDefinitions = resolveToolDefinitions(options, apiFormat);
        return requestConverter.toBaseChatRequest(prompt, options, apiFormat, toolDefinitions, false);
    }

    private ChatResponse internalCall(Prompt prompt, ChatResponse previousChatResponse) {
        OracleGenAiChatOptions options = (OracleGenAiChatOptions) prompt.getOptions();
        ChatRequest request = toChatRequest(prompt, options, false);
        ChatModelObservationContext observationContext = ChatModelObservationContext.builder()
                .prompt(prompt)
                .provider(OracleGenAiModelMetadata.PROVIDER)
                .build();
        ChatResponse chatResponse = ChatModelObservationDocumentation.CHAT_MODEL_OPERATION
                .observation(observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
                        observationRegistry)
                .observe(() -> {
                    com.oracle.bmc.generativeaiinference.responses.ChatResponse response =
                            RetryUtils.execute(retryTemplate, () -> client.chat(request));
                    ChatResponse convertedResponse =
                            withCumulativeUsage(responseConverter.toChatResponse(response), previousChatResponse);
                    observationContext.setResponse(convertedResponse);
                    return convertedResponse;
                });
        if (isInternalToolExecutionPossible(options)
                && toolExecutionEligibilityChecker.isToolCallResponse(chatResponse)) {
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
        Flux<ChatResponse> chatResponseFlux = observeStreamingCall(prompt, Flux.defer(() -> {
                    com.oracle.bmc.generativeaiinference.responses.ChatResponse response =
                            RetryUtils.execute(retryTemplate, () -> client.chat(request));
                    return streamResponseConverter.toChatResponses(response);
                })
                .map(chatResponse -> withStreamingMetadataDefaults(chatResponse, options))
                .map(chatResponse -> withCumulativeUsage(chatResponse, previousChatResponse))
                .subscribeOn(Schedulers.boundedElastic()));

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
                                || !toolExecutionEligibilityChecker.isToolCallResponse(chatResponse)) {
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

    private Flux<ChatResponse> observeStreamingCall(Prompt prompt, Flux<ChatResponse> chatResponseFlux) {
        return Flux.deferContextual(contextView -> {
            ChatModelObservationContext observationContext = ChatModelObservationContext.builder()
                    .prompt(prompt)
                    .provider(OracleGenAiModelMetadata.PROVIDER)
                    .streaming(true)
                    .build();
            Observation observation = ChatModelObservationDocumentation.CHAT_MODEL_OPERATION.observation(
                    observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
                    observationRegistry);
            observation.parentObservation(contextView.getOrDefault(ObservationThreadLocalAccessor.KEY, null)).start();
            return new MessageAggregator()
                    .aggregate(chatResponseFlux, observationContext::setResponse)
                    .doOnError(observation::error)
                    .doFinally(signalType -> observation.stop())
                    .contextWrite(context -> context.put(ObservationThreadLocalAccessor.KEY, observation));
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
        return !CollectionUtils.isEmpty(options.getToolCallbacks());
    }

    public static final class Builder {

        @Nullable
        private GenerativeAiInference client;

        @Nullable
        private OracleGenAiChatOptions defaultOptions;

        @Nullable
        private ToolCallingManager toolCallingManager;

        @Nullable
        private ToolExecutionEligibilityChecker toolExecutionEligibilityChecker;

        @Nullable
        private RetryTemplate retryTemplate;

        @Nullable
        private ObservationRegistry observationRegistry;

        @Nullable
        private ChatModelObservationConvention observationConvention;

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

        public Builder toolExecutionEligibilityChecker(
                ToolExecutionEligibilityChecker toolExecutionEligibilityChecker) {
            this.toolExecutionEligibilityChecker = toolExecutionEligibilityChecker;
            return this;
        }

        public Builder retryTemplate(RetryTemplate retryTemplate) {
            this.retryTemplate = retryTemplate;
            return this;
        }

        public Builder observationRegistry(ObservationRegistry observationRegistry) {
            this.observationRegistry = observationRegistry;
            return this;
        }

        public Builder observationConvention(ChatModelObservationConvention observationConvention) {
            this.observationConvention = observationConvention;
            return this;
        }

        public OracleGenAiChatModel build() {
            return new OracleGenAiChatModel(client, defaultOptions, toolCallingManager,
                    toolExecutionEligibilityChecker, retryTemplate, observationRegistry, observationConvention);
        }
    }

}
