/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import org.springframework.lang.Nullable;

/**
 * Exception thrown from any OCI storage related API invocation failures.
 */
public class StorageException extends RuntimeException {

    public StorageException(String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}