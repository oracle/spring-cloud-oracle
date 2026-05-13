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

    private String compartmentId;

    private ServingMode servingMode = ServingMode.ON_DEMAND;

    private String endpointId;

    private ApiFormat apiFormat;

    public enum ServingMode {
        ON_DEMAND,
        DEDICATED
    }

    public enum ApiFormat {
        GENERIC,
        COHERE_V2,
        COHERE
    }

    public static ApiFormat inferApiFormat(String model) {
        if (model != null && model.startsWith("cohere.command-a")) {
            return ApiFormat.COHERE_V2;
        }
        if (model != null && model.startsWith("cohere.")) {
            return ApiFormat.COHERE;
        }
        return ApiFormat.GENERIC;
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
            result.setCompartmentId(oracleOptions.getCompartmentId());
            result.setServingMode(oracleOptions.getServingMode());
            result.setEndpointId(oracleOptions.getEndpointId());
            result.setApiFormat(oracleOptions.getApiFormat());
        }
        return result;
    }

    public String getCompartmentId() {
        return compartmentId;
    }

    public void setCompartmentId(String compartmentId) {
        this.compartmentId = compartmentId;
    }

    public String getCompartment() {
        return getCompartmentId();
    }

    public void setCompartment(String compartment) {
        setCompartmentId(compartment);
    }

    public ServingMode getServingMode() {
        return servingMode;
    }

    public void setServingMode(ServingMode servingMode) {
        this.servingMode = servingMode;
    }

    @Override
    public boolean hasServingMode() {
        return servingMode != null;
    }

    @Override
    public boolean isDedicatedServingMode() {
        return servingMode == ServingMode.DEDICATED;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public ApiFormat getApiFormat() {
        return apiFormat;
    }

    public void setApiFormat(ApiFormat apiFormat) {
        this.apiFormat = apiFormat;
    }

    public List<String> getStop() {
        return getStopSequences();
    }

    public void setStop(List<String> stop) {
        setStopSequences(stop);
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
            if (oracleRuntimeOptions.getCompartmentId() != null) {
                merged.setCompartmentId(oracleRuntimeOptions.getCompartmentId());
            }
            if (oracleRuntimeOptions.getServingMode() != null) {
                merged.setServingMode(oracleRuntimeOptions.getServingMode());
            }
            if (oracleRuntimeOptions.getEndpointId() != null) {
                merged.setEndpointId(oracleRuntimeOptions.getEndpointId());
            }
            if (oracleRuntimeOptions.getApiFormat() != null) {
                merged.setApiFormat(oracleRuntimeOptions.getApiFormat());
            }
        }
        return merged;
    }

    private static void copyStandardOptionOverrides(org.springframework.ai.chat.prompt.ChatOptions source,
            OracleGenAiChatOptions target) {
        if (source.getModel() != null) {
            target.setModel(source.getModel());
        }
        if (source.getFrequencyPenalty() != null) {
            target.setFrequencyPenalty(source.getFrequencyPenalty());
        }
        if (source.getMaxTokens() != null) {
            target.setMaxTokens(source.getMaxTokens());
        }
        if (source.getPresencePenalty() != null) {
            target.setPresencePenalty(source.getPresencePenalty());
        }
        if (source.getStopSequences() != null) {
            target.setStopSequences(source.getStopSequences());
        }
        if (source.getTemperature() != null) {
            target.setTemperature(source.getTemperature());
        }
        if (source.getTopK() != null) {
            target.setTopK(source.getTopK());
        }
        if (source.getTopP() != null) {
            target.setTopP(source.getTopP());
        }
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

        public Builder servingMode(ServingMode servingMode) {
            options.setServingMode(servingMode);
            return this;
        }

        public Builder endpointId(String endpointId) {
            options.setEndpointId(endpointId);
            return this;
        }

        public Builder apiFormat(ApiFormat apiFormat) {
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
