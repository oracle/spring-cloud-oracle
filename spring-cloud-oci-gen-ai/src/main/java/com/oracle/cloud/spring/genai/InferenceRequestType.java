/*
 ** Copyright (c) 2024, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.genai;

import lombok.Getter;

/**
 * Supported OCI Inference model request types.
 */
@Getter
public enum InferenceRequestType {
    COHERE("COHERE"),
    LLAMA("LLAMA");

    private final String type;

    InferenceRequestType(String type) {
        this.type = type;
    }

}
