/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle.autoconfigure;

import com.oracle.spring.ai.oracle.OracleGenAiChatOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for OCI Generative AI chat.
 */
@ConfigurationProperties(prefix = PropertyNames.CHAT_CONFIG_PREFIX)
public class ChatProperties {
    private OracleGenAiChatOptions options = new OracleGenAiChatOptions();

    public OracleGenAiChatOptions getOptions() {
        return options;
    }

    public void setOptions(OracleGenAiChatOptions options) {
        this.options = options;
    }

    public String getModel() {
        return options.getModel();
    }

    public void setModel(String model) {
        options.setModel(model);
    }

    public String getCompartment() {
        return options.getCompartment();
    }

    public void setCompartment(String compartment) {
        options.setCompartment(compartment);
    }

    public String getCompartmentId() {
        return options.getCompartmentId();
    }

    public void setCompartmentId(String compartmentId) {
        options.setCompartmentId(compartmentId);
    }

    public OracleGenAiChatOptions.ServingMode getServingMode() {
        return options.getServingMode();
    }

    public void setServingMode(OracleGenAiChatOptions.ServingMode servingMode) {
        options.setServingMode(servingMode);
    }

    public String getEndpointId() {
        return options.getEndpointId();
    }

    public void setEndpointId(String endpointId) {
        options.setEndpointId(endpointId);
    }

    public OracleGenAiChatOptions.ApiFormat getApiFormat() {
        return options.getApiFormat();
    }

    public void setApiFormat(OracleGenAiChatOptions.ApiFormat apiFormat) {
        options.setApiFormat(apiFormat);
    }

    public Double getTemperature() {
        return options.getTemperature();
    }

    public void setTemperature(Double temperature) {
        options.setTemperature(temperature);
    }

    public Double getTopP() {
        return options.getTopP();
    }

    public void setTopP(Double topP) {
        options.setTopP(topP);
    }

    public Integer getTopK() {
        return options.getTopK();
    }

    public void setTopK(Integer topK) {
        options.setTopK(topK);
    }

    public Integer getMaxTokens() {
        return options.getMaxTokens();
    }

    public void setMaxTokens(Integer maxTokens) {
        options.setMaxTokens(maxTokens);
    }

    public Double getFrequencyPenalty() {
        return options.getFrequencyPenalty();
    }

    public void setFrequencyPenalty(Double frequencyPenalty) {
        options.setFrequencyPenalty(frequencyPenalty);
    }

    public Double getPresencePenalty() {
        return options.getPresencePenalty();
    }

    public void setPresencePenalty(Double presencePenalty) {
        options.setPresencePenalty(presencePenalty);
    }

    public java.util.List<String> getStop() {
        return options.getStop();
    }

    public void setStop(java.util.List<String> stop) {
        options.setStop(stop);
    }
}
