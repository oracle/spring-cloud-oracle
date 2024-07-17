/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StorageExceptionTests {
    @Test
    public void testException() {
        Exception exception = new StorageException("custom exception", new RuntimeException());
        assertEquals("custom exception", exception.getMessage());
    }
}
