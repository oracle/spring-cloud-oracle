/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import org.springframework.lang.Nullable;

/**
 * Content Type resolver based on file name.
 */
public interface StorageContentTypeResolver {

    @Nullable
    String resolveContentType(String fileName);
}
