/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle;

import com.oracle.spring.ai.oracle.api.OracleGenAiEmbeddingTruncate;
import com.oracle.spring.ai.oracle.api.OracleGenAiServingMode;
import org.springframework.ai.embedding.EmbeddingOptions;

/**
 * OCI Generative AI embedding options.
 */
public class OracleGenAiEmbeddingOptions implements EmbeddingOptions, OracleGenAiServingOptions {

    private String model;

    private Integer dimensions;

    private String compartmentId;

    private OracleGenAiServingMode servingMode = OracleGenAiServingMode.ON_DEMAND;

    private String endpointId;

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
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public Integer getDimensions() {
        return dimensions;
    }

    public void setDimensions(Integer dimensions) {
        this.dimensions = dimensions;
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
