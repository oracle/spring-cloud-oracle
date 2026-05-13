/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle;

import com.oracle.spring.ai.oracle.api.OracleGenAiServingMode;

final class OracleGenAiServingOptionsState {

    private String compartmentId;

    private OracleGenAiServingMode servingMode = OracleGenAiServingMode.ON_DEMAND;

    private String endpointId;

    String getCompartmentId() {
        return compartmentId;
    }

    void setCompartmentId(String compartmentId) {
        this.compartmentId = compartmentId;
    }

    OracleGenAiServingMode getServingMode() {
        return servingMode;
    }

    void setServingMode(OracleGenAiServingMode servingMode) {
        this.servingMode = servingMode;
    }

    String getEndpointId() {
        return endpointId;
    }

    void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }
}
