/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
        assertNotNull(StorageLocation.resolve("https://objectstorage.us-chicago-1.oraclecloud.com/n/namespace/b/mybucket/o/myobject"));
    }

    @Test
    void testStorageLocationWithInvalidInput() {
        assertNull(StorageLocation.resolve("https://foo.com"));
    }

    @Test
    void testSimpleStorageResource() {
        assertTrue(StorageLocation.isSimpleStorageResource("https://objectstorage.us-chicago-1.oraclecloud.com/n/namespace/b/mybucket/o/myobject"));
    }

    @Test
    void testResolveBucketName() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            StorageLocation.resolveBucketName("https://maacloud.objectstorage.us-chicago-1.oci.customer-oci.com/n/namespace");
        });
        assertTrue(exception.getMessage().contains(StorageLocation.ERROR_INVALID_BUCKET));
        String bucketName = StorageLocation.resolveBucketName("https://maacloud.objectstorage.us-chicago-1.oci.customer-oci.com/n/namespace/b/mybucket/o/myobject");
        assertThat(bucketName).isEqualTo("mybucket");
    }

    @Test
    void testResolveObjectName() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> StorageLocation.resolveObjectName("https://maacloud.objectstorage.us-chicago-1.oci.customer-oci.com/n/namespace/b/mybucket"));
        assertTrue(exception.getMessage().contains(StorageLocation.ERROR_OBJECT_REQUIRED));
        String objectName = StorageLocation.resolveObjectName("https://maacloud.objectstorage.us-chicago-1.oci.customer-oci.com/n/namespace/b/mybucket/o/myobject");
        assertThat(objectName).isEqualTo("myobject");
    }

    @Test
    void testStorageLocation() {
        StorageLocation storageLocation = new StorageLocation("testBucket", "testObject");
        assertEquals("testBucket", storageLocation.getBucket());
        assertEquals("testObject", storageLocation.getObject());
        assertNotNull(storageLocation.toString());
    }
}
