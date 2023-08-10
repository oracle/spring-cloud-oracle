/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StorageLocationTests {

    @Test
    public void testStorageLocationWithNullBucketName() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new StorageLocation(null, "Test Object");
        });
        assertTrue(StorageLocation.ERROR_BUCKET_REQUIRED.equals(exception.getMessage()));
    }

    @Test
    public void testStorageLocationWithNullObjectName() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new StorageLocation("Test Bucket", null);
        });
        assertTrue(StorageLocation.ERROR_OBJECT_REQUIRED.equals(exception.getMessage()));
    }

    @Test
    public void testStorageLocationWithValidInput() {
        assertNotNull(StorageLocation.resolve("ocs://test/bucket"));
    }

    @Test
    public void testStorageLocationWithInvalidInput() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assertNotNull(StorageLocation.resolve("ocs://test"));
        });
        assertTrue(exception.getMessage().contains(StorageLocation.ERROR_INVALID_BUCKET));
    }
}
