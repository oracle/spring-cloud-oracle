/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.responses.CreateBucketResponse;
import com.oracle.bmc.objectstorage.responses.GetNamespaceResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StorageImplTests {

    final ObjectStorageClient objectStorageClient = mock(ObjectStorageClient.class);
    final StorageObjectConverter storageObjectConverter = mock(StorageObjectConverter.class);
    final StorageContentTypeResolver storageContentTypeResolver = mock(StorageContentTypeResolver.class);

    @Test
    public void testStorageImplWithNullObjectStorageClient() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new StorageImpl(null, storageObjectConverter, storageContentTypeResolver, "compartment-ocid");
        });
        assertTrue(StorageImpl.ERROR_OSCLIENT_REQUIRED.equals(exception.getMessage()));
    }

    @Test
    public void testCreateBucket() {
        when(objectStorageClient.getNamespace(any())).thenReturn(mock(GetNamespaceResponse.class));
        when(objectStorageClient.createBucket(any())).thenReturn(mock(CreateBucketResponse.class));
        assertNotNull(getStorage().createBucket("testBucket"));
    }

    @Test
    public void testDownloadObject() {
        assertNotNull(getStorage().download("testObject", "testKey"));
    }

    @Test
    public void testDeleteObject() {
        when(objectStorageClient.getNamespace(any())).thenReturn(mock(GetNamespaceResponse.class));
        assertDoesNotThrow(() -> {
            getStorage().deleteObject("testObject", "testKey");
        });
    }

    @Test
    public void testDeleteBucket() {
        when(objectStorageClient.getNamespace(any())).thenReturn(mock(GetNamespaceResponse.class));
        assertDoesNotThrow(() -> {
            getStorage().deleteBucket("testBucket");
        });
    }

    private Storage getStorage() {
        return new StorageImpl(objectStorageClient, storageObjectConverter, storageContentTypeResolver, "compartment-ocid");
    }
}
