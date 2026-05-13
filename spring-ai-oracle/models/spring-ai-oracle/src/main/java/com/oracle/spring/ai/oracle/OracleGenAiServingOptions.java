/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle;

interface OracleGenAiServingOptions {

    String getCompartmentId();

    String getModel();

    String getEndpointId();

    boolean hasServingMode();

    boolean isDedicatedServingMode();
}
