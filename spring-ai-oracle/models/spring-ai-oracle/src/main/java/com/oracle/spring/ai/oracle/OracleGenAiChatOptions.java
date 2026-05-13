/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle;

import java.util.List;

import org.springframework.ai.chat.prompt.DefaultChatOptions;

/**
 * OCI Generative AI chat options.
 */
public class OracleGenAiChatOptions extends DefaultChatOptions implements OracleGenAiServingOptions {

    private final OracleGenAiServingOptionsState servingOptions = new OracleGenAiServingOptionsState();

    private OracleGenAiChatApiFormat apiFormat;

    public static OracleGenAiChatApiFormat inferApiFormat(String model) {
        return OracleGenAiChatApiFormat.infer(model);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static OracleGenAiChatOptions fromOptions(org.springframework.ai.chat.prompt.ChatOptions options) {
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
        if (options instanceof OracleGenAiChatOptions oracleOptions) {
            oracleOptions.copyServingOptionsTo(result);
            result.setApiFormat(oracleOptions.getApiFormat());
        }
        return result;
    }

    @Override
    public String getCompartmentId() {
        return servingOptions.getCompartmentId();
    }

    @Override
    public void setCompartmentId(String compartmentId) {
        servingOptions.setCompartmentId(compartmentId);
    }

    @Override
    public OracleGenAiServingMode getServingMode() {
        return servingOptions.getServingMode();
    }

    @Override
    public void setServingMode(OracleGenAiServingMode servingMode) {
        servingOptions.setServingMode(servingMode);
    }

    @Override
    public String getEndpointId() {
        return servingOptions.getEndpointId();
    }

    @Override
    public void setEndpointId(String endpointId) {
        servingOptions.setEndpointId(endpointId);
    }

    public OracleGenAiChatApiFormat getApiFormat() {
        return apiFormat;
    }

    public void setApiFormat(OracleGenAiChatApiFormat apiFormat) {
        this.apiFormat = apiFormat;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends org.springframework.ai.chat.prompt.ChatOptions> T copy() {
        return (T) fromOptions(this);
    }

    OracleGenAiChatOptions merge(org.springframework.ai.chat.prompt.ChatOptions runtimeOptions) {
        OracleGenAiChatOptions merged = copy();
        if (runtimeOptions == null) {
            return merged;
        }
        copyStandardOptionOverrides(runtimeOptions, merged);
        if (runtimeOptions instanceof OracleGenAiChatOptions oracleRuntimeOptions) {
            oracleRuntimeOptions.mergeServingOptionsTo(merged);
            OracleGenAiServingOptions.mergeOption(oracleRuntimeOptions.getApiFormat(), merged::setApiFormat);
        }
        return merged;
    }

    private static void copyStandardOptionOverrides(org.springframework.ai.chat.prompt.ChatOptions source,
            OracleGenAiChatOptions target) {
        OracleGenAiServingOptions.mergeOption(source.getModel(), target::setModel);
        OracleGenAiServingOptions.mergeOption(source.getFrequencyPenalty(), target::setFrequencyPenalty);
        OracleGenAiServingOptions.mergeOption(source.getMaxTokens(), target::setMaxTokens);
        OracleGenAiServingOptions.mergeOption(source.getPresencePenalty(), target::setPresencePenalty);
        OracleGenAiServingOptions.mergeOption(source.getStopSequences(), target::setStopSequences);
        OracleGenAiServingOptions.mergeOption(source.getTemperature(), target::setTemperature);
        OracleGenAiServingOptions.mergeOption(source.getTopK(), target::setTopK);
        OracleGenAiServingOptions.mergeOption(source.getTopP(), target::setTopP);
    }

    public static final class Builder {
        private final OracleGenAiChatOptions options = new OracleGenAiChatOptions();

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

        public Builder apiFormat(OracleGenAiChatApiFormat apiFormat) {
            options.setApiFormat(apiFormat);
            return this;
        }

        public Builder temperature(Double temperature) {
            options.setTemperature(temperature);
            return this;
        }

        public Builder topP(Double topP) {
            options.setTopP(topP);
            return this;
        }

        public Builder topK(Integer topK) {
            options.setTopK(topK);
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            options.setMaxTokens(maxTokens);
            return this;
        }

        public Builder frequencyPenalty(Double frequencyPenalty) {
            options.setFrequencyPenalty(frequencyPenalty);
            return this;
        }

        public Builder presencePenalty(Double presencePenalty) {
            options.setPresencePenalty(presencePenalty);
            return this;
        }

        public Builder stopSequences(List<String> stopSequences) {
            options.setStopSequences(stopSequences);
            return this;
        }

        public OracleGenAiChatOptions build() {
            return options.copy();
        }
    }
}
