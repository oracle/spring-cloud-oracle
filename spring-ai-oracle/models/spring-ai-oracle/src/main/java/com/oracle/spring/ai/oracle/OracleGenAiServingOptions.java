/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle;

import java.util.function.Consumer;

import com.oracle.bmc.generativeaiinference.model.DedicatedServingMode;
import com.oracle.bmc.generativeaiinference.model.OnDemandServingMode;
import com.oracle.bmc.generativeaiinference.model.ServingMode;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

interface OracleGenAiServingOptions {

    String getCompartmentId();

    void setCompartmentId(String compartmentId);

    String getModel();

    String getEndpointId();

    void setEndpointId(String endpointId);

    OracleGenAiServingMode getServingMode();

    void setServingMode(OracleGenAiServingMode servingMode);

    default boolean hasServingMode() {
        return getServingMode() != null;
    }

    default boolean isDedicatedServingMode() {
        return getServingMode() == OracleGenAiServingMode.DEDICATED;
    }

    default void validate() {
        Assert.hasText(getCompartmentId(), "OCI Generative AI compartmentId must be configured.");
        if (!hasServingMode()) {
            throw new IllegalArgumentException("OCI Generative AI servingMode must be configured.");
        }
        if (!isDedicatedServingMode() && !StringUtils.hasText(getModel())) {
            throw new IllegalArgumentException("OCI Generative AI on-demand serving mode requires options.model.");
        }
        if (isDedicatedServingMode() && !StringUtils.hasText(getEndpointId())) {
            throw new IllegalArgumentException("OCI Generative AI dedicated serving mode requires options.endpointId.");
        }
    }

    default void copyServingOptionsTo(OracleGenAiServingOptions target) {
        target.setCompartmentId(getCompartmentId());
        target.setServingMode(getServingMode());
        target.setEndpointId(getEndpointId());
    }

    default void mergeServingOptionsTo(OracleGenAiServingOptions target) {
        mergeOption(getCompartmentId(), target::setCompartmentId);
        mergeOption(getServingMode(), target::setServingMode);
        mergeOption(getEndpointId(), target::setEndpointId);
    }

    default ServingMode toServingMode() {
        if (isDedicatedServingMode()) {
            return DedicatedServingMode.builder().endpointId(getEndpointId()).build();
        }
        return OnDemandServingMode.builder().modelId(getModel()).build();
    }

    static <T> void mergeOption(T runtimeValue, Consumer<T> targetSetter) {
        if (runtimeValue != null) {
            targetSetter.accept(runtimeValue);
        }
    }
}
