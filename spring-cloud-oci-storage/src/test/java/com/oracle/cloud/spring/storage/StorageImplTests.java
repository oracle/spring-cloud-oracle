/*
 ** Copyright (c) 2023, 2024, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.responses.CreateBucketResponse;
import com.oracle.bmc.objectstorage.responses.GetNamespaceResponse;
import com.oracle.bmc.objectstorage.responses.PutObjectResponse;
import com.oracle.bmc.objectstorage.transfer.UploadManager;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// TODO: Needs fixing -- all test doesn't pass
class StorageImplTests {

    final ObjectStorageClient objectStorageClient = mock(ObjectStorageClient.class);
    final StorageObjectConverter storageObjectConverter = mock(StorageObjectConverter.class);
    final StorageContentTypeResolver storageContentTypeResolver = mock(StorageContentTypeResolver.class);
    final ObjectStorage objectStorage = mock(ObjectStorage.class);
    final Storage storage = new StorageImpl(objectStorageClient, storageObjectConverter,
            storageContentTypeResolver, "defaultCompartmentId");

    @Test
    void testStorageImplWithNullValues() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new StorageImpl(null, storageObjectConverter, storageContentTypeResolver, "compartmentId");
        });
        assertEquals(StorageImpl.ERROR_OSCLIENT_REQUIRED, exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> {
            new StorageImpl(objectStorageClient, null, storageContentTypeResolver, "compartmentId");
        });
        assertEquals(StorageImpl.ERROR_STORAGE_OBJECT_CONVERTER_REQUIRED, exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> {
            new StorageImpl(objectStorageClient, storageObjectConverter, null, "compartmentId");
        });
        assertEquals(StorageImpl.ERROR_CONTENT_TYPE_RESOLVER_REQUIRED, exception.getMessage());
    }

    @Test
    void testCreateBucket() {
        when(objectStorageClient.getNamespace(any())).thenReturn(mock(GetNamespaceResponse.class));
        when(objectStorageClient.createBucket(any())).thenReturn(mock(CreateBucketResponse.class));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            storage.createBucket(null, "compartmentId");
        });
        assertEquals(StorageImpl.ERROR_BUCKET_NAME_REQUIRED, exception.getMessage());
        exception = assertThrows(IllegalArgumentException.class, () -> {
            storage.createBucket("testBucket", null);
        });
        assertEquals(StorageImpl.ERROR_COMPARTMENT_REQUIRED, exception.getMessage());
        assertNotNull(storage.createBucket("testBucket"));
    }

    @Test
    void testDownloadObject() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            storage.download(null, "testKey");
        });
        assertEquals(StorageImpl.ERROR_BUCKET_NAME_REQUIRED, exception.getMessage());
        exception = assertThrows(IllegalArgumentException.class, () -> {
            storage.deleteObject("testObject", null);
        });
        assertEquals(StorageImpl.ERROR_KEY_REQUIRED, exception.getMessage());
        assertNotNull(storage.download("testObject", "testKey"));
    }

    @Test
    void testDeleteObject() {
        when(objectStorageClient.getNamespace(any())).thenReturn(mock(GetNamespaceResponse.class));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            storage.deleteObject(null, "testKey");
        });
        assertEquals(StorageImpl.ERROR_BUCKET_NAME_REQUIRED, exception.getMessage());
        exception = assertThrows(IllegalArgumentException.class, () -> {
            storage.deleteObject("testObject", null);
        });
        assertEquals(StorageImpl.ERROR_KEY_REQUIRED, exception.getMessage());
        assertDoesNotThrow(() -> {
            storage.deleteObject("testObject", "testKey");
        });
    }

    @Test
    void testDeleteBucket() {
        when(objectStorageClient.getNamespace(any())).thenReturn(mock(GetNamespaceResponse.class));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            storage.deleteBucket(null);
        });
        assertEquals(StorageImpl.ERROR_BUCKET_NAME_REQUIRED, exception.getMessage());
        assertDoesNotThrow(() -> {
            storage.deleteBucket("testBucket");
        });
    }

    @Test
    @Disabled
    void testStore() throws IOException {
        when(storageObjectConverter.write(any())).thenReturn("sample".getBytes());
        when(objectStorageClient.getNamespace(any())).thenReturn(mock(GetNamespaceResponse.class));
        when(mock(UploadManager.class).upload(any())).thenReturn(mock(UploadManager.UploadResponse.class));
        when(objectStorage.putObject(any())).thenReturn(mock(PutObjectResponse.class));
        assertNull(storage.store("bucketName", "key", "sample"));
    }

    @Test
    @Disabled
    void testUpload() throws IOException {
        when(storageObjectConverter.write(any())).thenReturn("sample".getBytes());
        when(objectStorageClient.getNamespace(any())).thenReturn(mock(GetNamespaceResponse.class));
        when(mock(UploadManager.class).upload(any())).thenReturn(mock(UploadManager.UploadResponse.class));
        when(objectStorage.putObject(any())).thenReturn(mock(PutObjectResponse.class));
        assertNull(storage.upload("bucketName", "key", new ByteArrayInputStream("sample".getBytes()), StorageObjectMetadata.builder().build()));
        assertNull(storage.upload("bucketName", "key", new ByteArrayInputStream("sample".getBytes()), StorageObjectMetadata.builder().contentType("application/json").build()));
    }

    @Test
    public void testDownload() {
        assertNotNull(storage.download("testBucket", "testKey"));
    }

    @Test
    @Disabled
    public void testRead() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            storage.read(null, "testKey", String.class);
        });
        assertEquals(StorageImpl.ERROR_BUCKET_NAME_REQUIRED, exception.getMessage());

        when(storageObjectConverter.read(any(), any())).thenThrow(mock(StorageException.class));
        assertThrows(StorageException.class, () -> {
            storage.read("testBucket", "testKey", String.class);
        });

        when(storageObjectConverter.read(any(), any())).thenReturn(mock(String.class));
        assertNotNull(storage.read("testBucket", "testKey", String.class));
    }

    @Test
    @Disabled
    public void testGetNamespaceName() {
        when(objectStorageClient.getNamespace(any())).thenReturn(mock(GetNamespaceResponse.class));
        assertNotNull(storage.getNamespaceName());
        assertNotNull(storage.getClient());
    }
}
