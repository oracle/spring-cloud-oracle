/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.oracle.spring.ai.oracle.api.GenAiApiFormat;
import com.oracle.spring.ai.oracle.api.OracleGenAiServingMode;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;

/**
 * OCI Generative AI chat options.
 */
public class OracleGenAiChatOptions implements OracleGenAiServingOptions, ToolCallingChatOptions {

    private String model;

    private Double frequencyPenalty;

    private Integer maxTokens;

    private Double presencePenalty;

    private List<String> stopSequences;

    private Double temperature;

    private Integer topK;

    private Double topP;

    private String compartmentId;

    private OracleGenAiServingMode servingMode = OracleGenAiServingMode.ON_DEMAND;

    private String endpointId;

    private List<ToolCallback> toolCallbacks = Collections.emptyList();

    private Map<String, Object> toolContext = Collections.emptyMap();

    private GenAiApiFormat apiFormat;

    public static Builder builder() {
        return new Builder();
    }

    public static OracleGenAiChatOptions fromOptions(ChatOptions options) {
        OracleGenAiChatOptions result = new OracleGenAiChatOptions();
        if (options == null) {
            return result;
        }
        result.setModel(options.getModel());
        result.setFrequencyPenalty(options.getFrequencyPenalty());
        result.setMaxTokens(options.getMaxTokens());
        result.setPresencePenalty(options.getPresencePenalty());
        result.setStopSequences(options.getStopSequences());
        result.setTemperature(options.getTemperature());
        result.setTopK(options.getTopK());
        result.setTopP(options.getTopP());
        if (options instanceof ToolCallingChatOptions toolCallingOptions) {
            result.setToolCallbacks(toolCallingOptions.getToolCallbacks());
            result.setToolContext(toolCallingOptions.getToolContext());
        }
        if (options instanceof OracleGenAiChatOptions oracleOptions) {
            oracleOptions.copyServingOptionsTo(result);
            result.setApiFormat(oracleOptions.getApiFormat());
        }
        return result;
    }

    @Override
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public Double getFrequencyPenalty() {
        return frequencyPenalty;
    }

    public void setFrequencyPenalty(Double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
    }

    @Override
    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    @Override
    public Double getPresencePenalty() {
        return presencePenalty;
    }

    public void setPresencePenalty(Double presencePenalty) {
        this.presencePenalty = presencePenalty;
    }

    @Override
    public List<String> getStopSequences() {
        return stopSequences;
    }

    public void setStopSequences(List<String> stopSequences) {
        this.stopSequences = stopSequences;
    }

    @Override
    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    @Override
    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    @Override
    public Double getTopP() {
        return topP;
    }

    public void setTopP(Double topP) {
        this.topP = topP;
    }

    @Override
    public String getCompartmentId() {
        return compartmentId;
    }

    @Override
    public void setCompartmentId(String compartmentId) {
        this.compartmentId = compartmentId;
    }

    @Override
    public OracleGenAiServingMode getServingMode() {
        return servingMode;
    }

    @Override
    public void setServingMode(OracleGenAiServingMode servingMode) {
        this.servingMode = servingMode;
    }

    @Override
    public String getEndpointId() {
        return endpointId;
    }

    @Override
    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public GenAiApiFormat getApiFormat() {
        return apiFormat;
    }

    public void setApiFormat(GenAiApiFormat apiFormat) {
        this.apiFormat = apiFormat;
    }

    @Override
    public List<ToolCallback> getToolCallbacks() {
        return toolCallbacks;
    }

    public void setToolCallbacks(List<ToolCallback> toolCallbacks) {
        this.toolCallbacks = toolCallbacks != null ? toolCallbacks : Collections.emptyList();
    }

    @Override
    public Map<String, Object> getToolContext() {
        return toolContext;
    }

    public void setToolContext(Map<String, Object> toolContext) {
        this.toolContext = toolContext != null ? toolContext : Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    public <T extends ChatOptions> T copy() {
        return (T) fromOptions(this);
    }

    @Override
    public Builder mutate() {
        return new Builder(this);
    }

    OracleGenAiChatOptions merge(ChatOptions runtimeOptions) {
        OracleGenAiChatOptions merged = copy();
        if (runtimeOptions == null) {
            return merged;
        }
        copyStandardOptionOverrides(runtimeOptions, merged);
        if (runtimeOptions instanceof ToolCallingChatOptions toolCallingRuntimeOptions) {
            merged.setToolCallbacks(ToolCallingChatOptions.mergeToolCallbacks(
                    toolCallingRuntimeOptions.getToolCallbacks(), merged.getToolCallbacks()));
            merged.setToolContext(ToolCallingChatOptions.mergeToolContext(
                    toolCallingRuntimeOptions.getToolContext(), merged.getToolContext()));
        }
        if (runtimeOptions instanceof OracleGenAiChatOptions oracleRuntimeOptions) {
            oracleRuntimeOptions.mergeServingOptionsTo(merged);
            OracleGenAiServingOptions.mergeOption(oracleRuntimeOptions.getApiFormat(), merged::setApiFormat);
        }
        return merged;
    }

    private static void copyStandardOptionOverrides(ChatOptions source, OracleGenAiChatOptions target) {
        OracleGenAiServingOptions.mergeOption(source.getModel(), target::setModel);
        OracleGenAiServingOptions.mergeOption(source.getFrequencyPenalty(), target::setFrequencyPenalty);
        OracleGenAiServingOptions.mergeOption(source.getMaxTokens(), target::setMaxTokens);
        OracleGenAiServingOptions.mergeOption(source.getPresencePenalty(), target::setPresencePenalty);
        OracleGenAiServingOptions.mergeOption(source.getStopSequences(), target::setStopSequences);
        OracleGenAiServingOptions.mergeOption(source.getTemperature(), target::setTemperature);
        OracleGenAiServingOptions.mergeOption(source.getTopK(), target::setTopK);
        OracleGenAiServingOptions.mergeOption(source.getTopP(), target::setTopP);
    }

    public static final class Builder implements ToolCallingChatOptions.Builder<Builder> {

        private OracleGenAiChatOptions options;

        public Builder() {
            this(new OracleGenAiChatOptions());
        }

        private Builder(OracleGenAiChatOptions options) {
            this.options = options.copy();
        }

        @Override
        public Builder model(String model) {
            options.setModel(model);
            return this;
        }

        public Builder compartmentId(String compartmentId) {
            options.setCompartmentId(compartmentId);
            return this;
        }

        public Builder servingMode(OracleGenAiServingMode servingMode) {
            options.setServingMode(servingMode);
            return this;
        }

        public Builder endpointId(String endpointId) {
            options.setEndpointId(endpointId);
            return this;
        }

        public Builder apiFormat(GenAiApiFormat apiFormat) {
            options.setApiFormat(apiFormat);
            return this;
        }

        @Override
        public Builder temperature(Double temperature) {
            options.setTemperature(temperature);
            return this;
        }

        @Override
        public Builder topP(Double topP) {
            options.setTopP(topP);
            return this;
        }

        @Override
        public Builder topK(Integer topK) {
            options.setTopK(topK);
            return this;
        }

        @Override
        public Builder maxTokens(Integer maxTokens) {
            options.setMaxTokens(maxTokens);
            return this;
        }

        @Override
        public Builder frequencyPenalty(Double frequencyPenalty) {
            options.setFrequencyPenalty(frequencyPenalty);
            return this;
        }

        @Override
        public Builder presencePenalty(Double presencePenalty) {
            options.setPresencePenalty(presencePenalty);
            return this;
        }

        @Override
        public Builder stopSequences(List<String> stopSequences) {
            options.setStopSequences(stopSequences);
            return this;
        }

        @Override
        public Builder toolCallbacks(List<ToolCallback> toolCallbacks) {
            options.setToolCallbacks(toolCallbacks);
            return this;
        }

        @Override
        public Builder toolCallbacks(ToolCallback... toolCallbacks) {
            return toolCallbacks(toolCallbacks != null ? List.of(toolCallbacks) : null);
        }

        @Override
        public Builder toolContext(Map<String, Object> toolContext) {
            options.setToolContext(toolContext);
            return this;
        }

        @Override
        public Builder toolContext(String key, Object value) {
            Map<String, Object> toolContext = new LinkedHashMap<>(options.getToolContext());
            toolContext.put(key, value);
            options.setToolContext(toolContext);
            return this;
        }

        @Override
        public Builder clone() {
            return new Builder(options);
        }

        @Override
        public Builder combineWith(ChatOptions.Builder<?> builder) {
            if (builder != null) {
                options = options.merge(builder.build());
            }
            return this;
        }

        @Override
        public OracleGenAiChatOptions build() {
            return options.copy();
        }
    }
}
