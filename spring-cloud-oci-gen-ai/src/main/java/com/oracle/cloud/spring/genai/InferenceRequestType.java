/*
 ** Copyright (c) 2024, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.genai;

public enum InferenceRequestType {
    COHERE("COHERE"),
    LLAMA("LLAMA");

    private final String type;

    InferenceRequestType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
