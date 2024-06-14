/*
 ** Copyright (c) 2024, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.genai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = GenAIProperties.PREFIX)
public class GenAIProperties {
    public static final String PREFIX = "spring.cloud.oci.genai";

    private Chat chat;
    private Embedding embedding;

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public Embedding getEmbedding() {
        return embedding;
    }

    public void setEmbedding(Embedding embedding) {
        this.embedding = embedding;
    }

    public static class Chat {
        private String onDemandModelId;
        private String dedicatedClusterEndpoint;
        private String compartment;
        private String preambleOverride;
        private Double temperature;
        private Double frequencyPenalty;
        private Integer maxTokens;
        private Double presencePenalty;
        private Double topP;
        private Integer topK;
        private InferenceRequestType inferenceRequestType;

        public String getOnDemandModelId() {
            return onDemandModelId;
        }

        public void setOnDemandModelId(String onDemandModelId) {
            this.onDemandModelId = onDemandModelId;
        }

        public String getDedicatedClusterEndpoint() {
            return dedicatedClusterEndpoint;
        }

        public void setDedicatedClusterEndpoint(String dedicatedClusterEndpoint) {
            this.dedicatedClusterEndpoint = dedicatedClusterEndpoint;
        }

        public String getCompartment() {
            return compartment;
        }

        public void setCompartment(String compartment) {
            this.compartment = compartment;
        }

        public String getPreambleOverride() {
            return preambleOverride;
        }

        public void setPreambleOverride(String preambleOverride) {
            this.preambleOverride = preambleOverride;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Double getFrequencyPenalty() {
            return frequencyPenalty;
        }

        public void setFrequencyPenalty(Double frequencyPenalty) {
            this.frequencyPenalty = frequencyPenalty;
        }

        public Integer getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
        }

        public Double getPresencePenalty() {
            return presencePenalty;
        }

        public void setPresencePenalty(Double presencePenalty) {
            this.presencePenalty = presencePenalty;
        }

        public Double getTopP() {
            return topP;
        }

        public void setTopP(Double topP) {
            this.topP = topP;
        }

        public Integer getTopK() {
            return topK;
        }

        public void setTopK(Integer topK) {
            this.topK = topK;
        }

        public InferenceRequestType getInferenceRequestType() {
            return inferenceRequestType;
        }

        public void setInferenceRequestType(InferenceRequestType inferenceRequestType) {
            this.inferenceRequestType = inferenceRequestType;
        }
    }

    public static class Embedding {
        private String onDemandModelId;
        private String dedicatedClusterEndpoint;
        private String compartment;
        private String truncate;

        public String getOnDemandModelId() {
            return onDemandModelId;
        }

        public void setOnDemandModelId(String onDemandModelId) {
            this.onDemandModelId = onDemandModelId;
        }

        public String getDedicatedClusterEndpoint() {
            return dedicatedClusterEndpoint;
        }

        public void setDedicatedClusterEndpoint(String dedicatedClusterEndpoint) {
            this.dedicatedClusterEndpoint = dedicatedClusterEndpoint;
        }

        public String getCompartment() {
            return compartment;
        }

        public void setCompartment(String compartment) {
            this.compartment = compartment;
        }

        public String getTruncate() {
            return truncate;
        }

        public void setTruncate(String truncate) {
            this.truncate = truncate;
        }
    }
}
