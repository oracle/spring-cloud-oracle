/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle.autoconfigure;

import com.oracle.spring.ai.oracle.OracleGenAiEmbeddingOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for OCI Generative AI embeddings.
 */
@ConfigurationProperties(prefix = PropertyNames.EMBEDDING_CONFIG_PREFIX)
public class EmbeddingProperties {


    private OracleGenAiEmbeddingOptions options = new OracleGenAiEmbeddingOptions();

    public OracleGenAiEmbeddingOptions getOptions() {
        return options;
    }

    public void setOptions(OracleGenAiEmbeddingOptions options) {
        this.options = options;
    }

    public String getModel() {
        return options.getModel();
    }

    public void setModel(String model) {
        options.setModel(model);
    }

    public Integer getDimensions() {
        return options.getDimensions();
    }

    public void setDimensions(Integer dimensions) {
        options.setDimensions(dimensions);
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

    public OracleGenAiEmbeddingOptions.ServingMode getServingMode() {
        return options.getServingMode();
    }

    public void setServingMode(OracleGenAiEmbeddingOptions.ServingMode servingMode) {
        options.setServingMode(servingMode);
    }

    public String getEndpointId() {
        return options.getEndpointId();
    }

    public void setEndpointId(String endpointId) {
        options.setEndpointId(endpointId);
    }

    public OracleGenAiEmbeddingOptions.Truncate getTruncate() {
        return options.getTruncate();
    }

    public void setTruncate(OracleGenAiEmbeddingOptions.Truncate truncate) {
        options.setTruncate(truncate);
    }
}
