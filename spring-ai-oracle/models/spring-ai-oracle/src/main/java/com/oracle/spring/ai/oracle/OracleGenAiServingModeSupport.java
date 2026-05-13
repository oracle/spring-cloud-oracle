/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle;

import com.oracle.bmc.generativeaiinference.model.DedicatedServingMode;
import com.oracle.bmc.generativeaiinference.model.OnDemandServingMode;
import com.oracle.bmc.generativeaiinference.model.ServingMode;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

final class OracleGenAiServingModeSupport {

    private OracleGenAiServingModeSupport() {
    }

    static void validate(OracleGenAiServingOptions options) {
        Assert.hasText(options.getCompartmentId(), "OCI Generative AI compartmentId must be configured.");
        if (!options.hasServingMode()) {
            throw new IllegalArgumentException("OCI Generative AI servingMode must be configured.");
        }
        if (!options.isDedicatedServingMode() && !StringUtils.hasText(options.getModel())) {
            throw new IllegalArgumentException("OCI Generative AI on-demand serving mode requires options.model.");
        }
        if (options.isDedicatedServingMode() && !StringUtils.hasText(options.getEndpointId())) {
            throw new IllegalArgumentException("OCI Generative AI dedicated serving mode requires options.endpointId.");
        }
    }

    static ServingMode toServingMode(OracleGenAiServingOptions options) {
        if (options.isDedicatedServingMode()) {
            return DedicatedServingMode.builder().endpointId(options.getEndpointId()).build();
        }
        return OnDemandServingMode.builder().modelId(options.getModel()).build();
    }
}
