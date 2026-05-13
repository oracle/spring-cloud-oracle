/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.spring.ai.oracle;

/**
 * OCI Generative AI chat request format.
 */
public enum OracleGenAiChatApiFormat {
    GENERIC,
    COHERE_V2,
    COHERE;

    public static OracleGenAiChatApiFormat infer(String model) {
        if (model != null && model.startsWith("cohere.command-a")) {
            return COHERE_V2;
        }
        if (model != null && model.startsWith("cohere.")) {
            return COHERE;
        }
        return GENERIC;
    }
}
