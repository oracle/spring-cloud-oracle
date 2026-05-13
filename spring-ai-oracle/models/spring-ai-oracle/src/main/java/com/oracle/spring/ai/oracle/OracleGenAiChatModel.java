/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle;

import java.util.Collections;
import java.util.List;

import com.oracle.bmc.generativeaiinference.GenerativeAiInference;
import com.oracle.bmc.generativeaiinference.model.BaseChatRequest;
import com.oracle.bmc.generativeaiinference.requests.ChatRequest;
import com.oracle.spring.ai.oracle.api.GenAiApiFormat;
import com.oracle.spring.ai.oracle.converter.ChatRequestConverter;
import com.oracle.spring.ai.oracle.converter.ChatResponseConverter;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.DefaultToolExecutionEligibilityPredicate;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionEligibilityPredicate;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import static com.oracle.spring.ai.oracle.api.GenAiApiFormat.infer;

/**
 * Spring AI chat model backed by OCI Generative AI.
 */
public class OracleGenAiChatModel implements ChatModel {

    private static final ToolCallingManager DEFAULT_TOOL_CALLING_MANAGER = ToolCallingManager.builder().build();

    private final GenerativeAiInference client;

    private final OracleGenAiChatOptions defaultOptions;

    private final ToolCallingManager toolCallingManager;

    private final ToolExecutionEligibilityPredicate toolExecutionEligibilityPredicate;

    private final RetryTemplate retryTemplate;

    private final ChatRequestConverter requestConverter = new ChatRequestConverter();

    private final ChatResponseConverter responseConverter = new ChatResponseConverter();

    public OracleGenAiChatModel(GenerativeAiInference client, OracleGenAiChatOptions defaultOptions) {
        this(client, defaultOptions, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public OracleGenAiChatModel(GenerativeAiInference client, OracleGenAiChatOptions defaultOptions,
            RetryTemplate retryTemplate) {
        this(client, defaultOptions, DEFAULT_TOOL_CALLING_MANAGER, retryTemplate);
    }

    public OracleGenAiChatModel(GenerativeAiInference client, OracleGenAiChatOptions defaultOptions,
            ToolCallingManager toolCallingManager, RetryTemplate retryTemplate) {
        this(client, defaultOptions, toolCallingManager, new DefaultToolExecutionEligibilityPredicate(), retryTemplate);
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
        return internalCall(new Prompt(prompt.getInstructions(), defaultOptions.merge(prompt.getOptions())));
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
        options.validate();
        return requestConverter.toChatRequest(options, toBaseChatRequest(prompt, options));
    }

    BaseChatRequest toBaseChatRequest(Prompt prompt, OracleGenAiChatOptions options) {
        GenAiApiFormat apiFormat = options.getApiFormat() != null ? options.getApiFormat() : infer(options.getModel());
        List<ToolDefinition> toolDefinitions = resolveToolDefinitions(options, apiFormat);
        return requestConverter.toBaseChatRequest(prompt, options, apiFormat, toolDefinitions);
    }

    private ChatResponse internalCall(Prompt prompt) {
        OracleGenAiChatOptions options = (OracleGenAiChatOptions) prompt.getOptions();
        ChatRequest request = toChatRequest(prompt, options);
        com.oracle.bmc.generativeaiinference.responses.ChatResponse response =
                RetryUtils.execute(retryTemplate, () -> client.chat(request));
        ChatResponse chatResponse = responseConverter.toChatResponse(response);
        if (toolExecutionEligibilityPredicate.isToolExecutionRequired(options, chatResponse)) {
            ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, chatResponse);
            if (toolExecutionResult.returnDirect()) {
                return ChatResponse.builder()
                        .from(chatResponse)
                        .generations(ToolExecutionResult.buildGenerations(toolExecutionResult))
                        .build();
            }
            return internalCall(new Prompt(toolExecutionResult.conversationHistory(), options));
        }
        return chatResponse;
    }

    private List<ToolDefinition> resolveToolDefinitions(OracleGenAiChatOptions options,
            GenAiApiFormat apiFormat) {
        if (apiFormat == GenAiApiFormat.COHERE) {
            return Collections.emptyList();
        }
        ToolCallingChatOptions.validateToolCallbacks(options.getToolCallbacks());
        return toolCallingManager.resolveToolDefinitions(options);
    }

}
