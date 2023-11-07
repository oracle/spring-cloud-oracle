/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StorageLocationTests {

    @Test
    void testStorageLocationWithNullBucketName() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new StorageLocation(null, "Test Object");
        });
        assertEquals(StorageLocation.ERROR_BUCKET_REQUIRED, exception.getMessage());
    }

    @Test
    void testStorageLocationWithNullObjectName() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new StorageLocation("Test Bucket", null);
        });
        assertEquals(StorageLocation.ERROR_OBJECT_REQUIRED, exception.getMessage());
    }

    @Test
    void testStorageLocationWithValidInput() {
        assertNotNull(StorageLocation.resolve("ocs://test/bucket"));
    }

    @Test
    void testStorageLocationWithInvalidInput() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            StorageLocation.resolve("ocs://test");
        });
        assertTrue(exception.getMessage().contains(StorageLocation.ERROR_INVALID_BUCKET));
    }

    @Test
    void testSimpleStorageResource() {
        assertTrue(StorageLocation.isSimpleStorageResource("ocs://test/bucket"));
    }

    @Test
    void testResolveBucketName() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            StorageLocation.resolveBucketName("ocs://test");
        });
        assertTrue(exception.getMessage().contains(StorageLocation.ERROR_INVALID_BUCKET));
        assertNotNull(StorageLocation.resolveBucketName("ocs://test/bucket"));
    }

    @Test
    void testResolveObjectName() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            StorageLocation.resolveObjectName("ocs://test");
        });
        assertTrue(exception.getMessage().contains(StorageLocation.ERROR_INVALID_BUCKET));
        assertNotNull(StorageLocation.resolveObjectName("ocs://test/bucket/^"));
    }

    @Test
    void testResolveVersionId() {
        assertNull(StorageLocation.resolveVersionId("ocs://test/bucket/v1"));
    }

    @Test
    void testStorageLocation() {
        StorageLocation storageLocation = new StorageLocation("testBucket", "testObject", "v1");
        assertEquals("testBucket", storageLocation.getBucket());
        assertEquals("testObject", storageLocation.getObject());
        assertEquals("v1", storageLocation.getVersion());
        assertNotNull(storageLocation.toString());
    }
}
