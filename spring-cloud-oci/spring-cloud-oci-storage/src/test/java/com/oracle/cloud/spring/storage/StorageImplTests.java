/*
 ** Copyright (c) 2023, 2026, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StorageImplTests {

    final StorageTestSupport.FakeObjectStorageClient objectStorageClient = StorageTestSupport.newObjectStorageClient();
    final StorageTestSupport.RecordingStorageObjectConverter storageObjectConverter =
            new StorageTestSupport.RecordingStorageObjectConverter();
    final StorageTestSupport.RecordingContentTypeResolver storageContentTypeResolver =
            new StorageTestSupport.RecordingContentTypeResolver("text/plain");
    final StorageTestSupport.RecordingStorageObjectUploader objectUploader =
            new StorageTestSupport.RecordingStorageObjectUploader();
    final Storage storage = new StorageImpl(objectStorageClient, storageObjectConverter,
            storageContentTypeResolver, "defaultCompartmentId", objectUploader);

    @Test
    void testStorageImplWithNullValues() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new StorageImpl(null, storageObjectConverter, storageContentTypeResolver, "compartmentId"));
        assertEquals(StorageImpl.ERROR_OSCLIENT_REQUIRED, exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () ->
                new StorageImpl(objectStorageClient, null, storageContentTypeResolver, "compartmentId"));
        assertEquals(StorageImpl.ERROR_STORAGE_OBJECT_CONVERTER_REQUIRED, exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () ->
                new StorageImpl(objectStorageClient, storageObjectConverter, null, "compartmentId"));
        assertEquals(StorageImpl.ERROR_CONTENT_TYPE_RESOLVER_REQUIRED, exception.getMessage());
    }

    @Test
    void testCreateBucket() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> storage.createBucket(null, "compartmentId"));
        assertEquals(StorageImpl.ERROR_BUCKET_NAME_REQUIRED, exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> storage.createBucket("testBucket", null));
        assertEquals(StorageImpl.ERROR_COMPARTMENT_REQUIRED, exception.getMessage());

        assertNotNull(storage.createBucket("testBucket"));
        assertEquals("testBucket", objectStorageClient.lastCreateBucketRequest.getCreateBucketDetails().getName());
    }

    @Test
    void testDownloadObject() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> storage.download(null, "testKey"));
        assertEquals(StorageImpl.ERROR_BUCKET_NAME_REQUIRED, exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> storage.deleteObject("testObject", null));
        assertEquals(StorageImpl.ERROR_KEY_REQUIRED, exception.getMessage());

        assertNotNull(storage.download("testObject", "testKey"));
    }

    @Test
    void testDeleteObject() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> storage.deleteObject(null, "testKey"));
        assertEquals(StorageImpl.ERROR_BUCKET_NAME_REQUIRED, exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> storage.deleteObject("testObject", null));
        assertEquals(StorageImpl.ERROR_KEY_REQUIRED, exception.getMessage());

        assertDoesNotThrow(() -> storage.deleteObject("testObject", "testKey"));
        assertEquals("testObject", objectStorageClient.lastDeleteObjectRequest.getBucketName());
        assertEquals("testKey", objectStorageClient.lastDeleteObjectRequest.getObjectName());
    }

    @Test
    void testDeleteBucket() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> storage.deleteBucket(null));
        assertEquals(StorageImpl.ERROR_BUCKET_NAME_REQUIRED, exception.getMessage());

        assertDoesNotThrow(() -> storage.deleteBucket("testBucket"));
        assertEquals("testBucket", objectStorageClient.lastDeleteBucketRequest.getBucketName());
    }

    @Test
    void testStore() throws IOException {
        assertNotNull(storage.store("bucketName", "key", "sample"));
        assertEquals("sample", new String(objectUploader.lastContent, StandardCharsets.UTF_8));
        assertEquals("text/plain", objectUploader.lastRequest.getContentType());
        assertEquals("sample", storageObjectConverter.lastWrittenObject);
    }

    @Test
    void testUpload() throws IOException {
        assertNotNull(storage.upload("bucketName", "key",
                new ByteArrayInputStream("sample".getBytes(StandardCharsets.UTF_8)),
                StorageObjectMetadata.builder().build()));
        assertEquals("sample", new String(objectUploader.lastContent, StandardCharsets.UTF_8));
        assertEquals("text/plain", objectUploader.lastRequest.getContentType());
        assertEquals("key", storageContentTypeResolver.lastObjectName);

        assertNotNull(storage.upload("bucketName", "key",
                new ByteArrayInputStream("sample".getBytes(StandardCharsets.UTF_8)),
                StorageObjectMetadata.builder().contentType("application/json").build()));
        assertEquals("application/json", objectUploader.lastRequest.getContentType());
    }

    @Test
    void testDownload() {
        assertNotNull(storage.download("testBucket", "testKey"));
    }

    @Test
    void testRead() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> storage.read(null, "testKey", String.class));
        assertEquals(StorageImpl.ERROR_BUCKET_NAME_REQUIRED, exception.getMessage());

        storageObjectConverter.readException = new StorageException("boom", new RuntimeException("cause"));
        assertThrows(StorageException.class, () -> storage.read("testBucket", "testKey", String.class));

        storageObjectConverter.readException = null;
        storageObjectConverter.readValue = "read-value";
        assertEquals("read-value", storage.read("testBucket", "testKey", String.class));
    }

    @Test
    void testGetNamespaceName() {
        assertEquals("testNamespace", storage.getNamespaceName());
        assertNotNull(storage.getClient());
    }
}
