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

    private String compartmentId;

    private ServingMode servingMode = ServingMode.ON_DEMAND;

    private String endpointId;

    private Truncate truncate = Truncate.NONE;

    public enum ServingMode {
        ON_DEMAND,
        DEDICATED
    }

    public enum Truncate {
        NONE,
        START,
        END
    }

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
            result.setCompartmentId(oracleOptions.getCompartmentId());
            result.setServingMode(oracleOptions.getServingMode());
            result.setEndpointId(oracleOptions.getEndpointId());
            result.setTruncate(oracleOptions.getTruncate());
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

    public Truncate getTruncate() {
        return truncate;
    }

    public void setTruncate(Truncate truncate) {
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
        if (runtimeOptions.getModel() != null) {
            merged.setModel(runtimeOptions.getModel());
        }
        if (runtimeOptions.getDimensions() != null) {
            merged.setDimensions(runtimeOptions.getDimensions());
        }
        if (runtimeOptions instanceof OracleGenAiEmbeddingOptions oracleRuntimeOptions) {
            if (oracleRuntimeOptions.getCompartmentId() != null) {
                merged.setCompartmentId(oracleRuntimeOptions.getCompartmentId());
            }
            if (oracleRuntimeOptions.getServingMode() != null) {
                merged.setServingMode(oracleRuntimeOptions.getServingMode());
            }
            if (oracleRuntimeOptions.getEndpointId() != null) {
                merged.setEndpointId(oracleRuntimeOptions.getEndpointId());
            }
            if (oracleRuntimeOptions.getTruncate() != null) {
                merged.setTruncate(oracleRuntimeOptions.getTruncate());
            }
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

        public Builder servingMode(ServingMode servingMode) {
            options.setServingMode(servingMode);
            return this;
        }

        public Builder endpointId(String endpointId) {
            options.setEndpointId(endpointId);
            return this;
        }

        public Builder truncate(Truncate truncate) {
            options.setTruncate(truncate);
            return this;
        }

        public OracleGenAiEmbeddingOptions build() {
            return options.copy();
        }
    }
}
