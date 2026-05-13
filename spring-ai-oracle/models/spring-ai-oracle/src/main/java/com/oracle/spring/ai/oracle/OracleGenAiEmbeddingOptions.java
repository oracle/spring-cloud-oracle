/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle;

import org.springframework.ai.embedding.DefaultEmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingOptions;

/**
 * OCI Generative AI embedding options.
 */
public class OracleGenAiEmbeddingOptions extends DefaultEmbeddingOptions implements OracleGenAiServingOptions {

    private final OracleGenAiServingOptionsState servingOptions = new OracleGenAiServingOptionsState();

    private OracleGenAiEmbeddingTruncate truncate = OracleGenAiEmbeddingTruncate.NONE;

    public static Builder builder() {
        return new Builder();
    }

    public static OracleGenAiEmbeddingOptions fromOptions(EmbeddingOptions options) {
        OracleGenAiEmbeddingOptions result = new OracleGenAiEmbeddingOptions();
        if (options == null) {
            return result;
        }
        result.setModel(options.getModel());
        result.setDimensions(options.getDimensions());
        if (options instanceof OracleGenAiEmbeddingOptions oracleOptions) {
            oracleOptions.copyServingOptionsTo(result);
            result.setTruncate(oracleOptions.getTruncate());
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

    public OracleGenAiEmbeddingTruncate getTruncate() {
        return truncate;
    }

    public void setTruncate(OracleGenAiEmbeddingTruncate truncate) {
        this.truncate = truncate;
    }

    @SuppressWarnings("unchecked")
    public <T extends EmbeddingOptions> T copy() {
        return (T) fromOptions(this);
    }

    OracleGenAiEmbeddingOptions merge(EmbeddingOptions runtimeOptions) {
        OracleGenAiEmbeddingOptions merged = copy();
        if (runtimeOptions == null) {
            return merged;
        }
        OracleGenAiServingOptions.mergeOption(runtimeOptions.getModel(), merged::setModel);
        OracleGenAiServingOptions.mergeOption(runtimeOptions.getDimensions(), merged::setDimensions);
        if (runtimeOptions instanceof OracleGenAiEmbeddingOptions oracleRuntimeOptions) {
            oracleRuntimeOptions.mergeServingOptionsTo(merged);
            OracleGenAiServingOptions.mergeOption(oracleRuntimeOptions.getTruncate(), merged::setTruncate);
        }
        return merged;
    }

    public static final class Builder {
        private final OracleGenAiEmbeddingOptions options = new OracleGenAiEmbeddingOptions();

        public Builder model(String model) {
            options.setModel(model);
            return this;
        }

        public Builder dimensions(Integer dimensions) {
            options.setDimensions(dimensions);
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

        public Builder truncate(OracleGenAiEmbeddingTruncate truncate) {
            options.setTruncate(truncate);
            return this;
        }

        public OracleGenAiEmbeddingOptions build() {
            return options.copy();
        }
    }
}
